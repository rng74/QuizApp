package kz.yers.quiz

import android.os.SystemClock
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kz.yers.quiz.data.local.dao.DailyAttemptDao
import kz.yers.quiz.data.local.dao.RunHistoryDao
import kz.yers.quiz.data.local.entity.DailyAttemptEntity
import kz.yers.quiz.data.local.entity.RunHistoryEntity
import kz.yers.quiz.data.prefs.UserPrefs
import kz.yers.quiz.data.remote.DailyStatsRepository
import kz.yers.quiz.data.remote.DuelCreateResult
import kz.yers.quiz.data.remote.DuelJoinResult
import kz.yers.quiz.data.remote.DuelRepository
import kz.yers.quiz.data.remote.LeaderboardRepository
import kz.yers.quiz.model.A11yState
import kz.yers.quiz.model.Achievements
import kz.yers.quiz.model.AppState
import kz.yers.quiz.model.DailyAttemptSummary
import kz.yers.quiz.model.DailyState
import kz.yers.quiz.model.DuelPhase
import kz.yers.quiz.model.DuelRole
import kz.yers.quiz.model.DuelState
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.model.HintInventory
import kz.yers.quiz.model.HintType
import kz.yers.quiz.model.LeaderboardUiState
import kz.yers.quiz.model.ProfileState
import kz.yers.quiz.model.QuestionHintState
import kz.yers.quiz.model.QuizQuestion
import kz.yers.quiz.model.RecentGame
import kz.yers.quiz.model.UserStats
import kz.yers.quiz.repo.AnimeRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class QuizAppViewModel(
    private val repository: AnimeRepository,
    private val runHistoryDao: RunHistoryDao,
    private val dailyAttemptDao: DailyAttemptDao,
    private val userPrefs: UserPrefs,
    private val leaderboard: LeaderboardRepository,
    private val dailyStats: DailyStatsRepository,
    private val duel: DuelRepository,
) : ViewModel() {
    var appState = mutableStateOf<AppState>(AppState.Menu)

    private var quizQuestions: List<QuizQuestion> = emptyList()
    private var runStartElapsedMs: Long = 0L

    private val _highScore = mutableIntStateOf(0)
    val highScore: State<Int> = _highScore

    val score = mutableIntStateOf(0)

    var tries = 0

    val userAnswer = mutableStateOf<String?>(null)

    val timeRemaining = mutableLongStateOf(10_000L)

    val maxTimePerQuestion = 10_000L

    private var timerJob: Job? = null

    private var isTimerRunning = mutableStateOf(false)

    private val _isPosterEnabled = mutableStateOf(true)
    val isPosterEnabled: State<Boolean> = _isPosterEnabled

    val activeMode = mutableStateOf<GameMode?>(null)

    val isNewRecord = mutableStateOf(false)

    val needsOnboarding = mutableStateOf(false)
    val onboardingResolved = mutableStateOf(false)

    val dailyState = mutableStateOf(DailyState())
    val profileState = mutableStateOf(ProfileState())

    val a11y = mutableStateOf(A11yState())
    val soundEnabled = mutableStateOf(true)

    private var isDailyRun = false
    private var isDuelRun = false

    val duelState = mutableStateOf(DuelState())
    private var duelListenerJob: Job? = null
    val totalQuestionsInRun = mutableIntStateOf(30)

    val coins = mutableIntStateOf(240)
    val streakDays = mutableIntStateOf(0)
    val bestScoreByMode = mutableStateOf<Map<GameMode, Int>>(emptyMap())
    val recordModeJustSet = mutableStateOf<GameMode?>(null)

    val hintInventory = mutableStateOf(HintInventory())
    val questionHintState = mutableStateOf(QuestionHintState())
    val pendingRewardedAd = mutableStateOf<HintType?>(null)
    val resultWasDaily = mutableStateOf(false)
    val lastRunCoins = mutableIntStateOf(0)
    val lastRunCorrect = mutableIntStateOf(0)
    val lastRunTotal = mutableIntStateOf(0)
    private var skipRequested = false

    init {
        _highScore.intValue = repository.getHighScore()
        viewModelScope.launch {
            _isPosterEnabled.value = userPrefs.posterEnabled.first()
            soundEnabled.value = userPrefs.soundEnabled.first()
            a11y.value =
                A11yState(
                    reduceMotion = userPrefs.reduceMotion.first(),
                    colorBlindSafe = userPrefs.colorBlindSafe.first(),
                    largerText = userPrefs.largerText.first(),
                    dyslexiaFont = userPrefs.dyslexiaFont.first(),
                )
            val tutorialDone = userPrefs.tutorialCompleted.first()
            needsOnboarding.value = !tutorialDone
            onboardingResolved.value = true
            hintInventory.value =
                HintInventory(
                    fiftyFifty = userPrefs.hintsFiftyFifty.first(),
                    revealLetter = userPrefs.hintsReveal.first(),
                    skip = userPrefs.hintsSkip.first(),
                )
            coins.intValue = userPrefs.coins.first()
            refreshMenuStats()
        }
    }

    private suspend fun refreshMenuStats() {
        bestScoreByMode.value =
            GameMode.entries.associateWith { mode ->
                withContext(Dispatchers.IO) { runHistoryDao.bestScoreForMode(mode.name) } ?: 0
            }
        streakDays.intValue = userPrefs.currentStreakDays.first()
        coins.intValue = userPrefs.coins.first()
        refreshDailyLiveStats()
    }

    val leaderboardState = mutableStateOf<LeaderboardUiState>(LeaderboardUiState.Loading)

    fun openLeaderboard() {
        appState.value = AppState.Leaderboard
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        leaderboardState.value = LeaderboardUiState.Loading
        viewModelScope.launch {
            leaderboardState.value = leaderboard.load()
        }
    }

    val hintsAvailableForCurrentRun: Boolean
        get() = !isDailyRun && !isDuelRun

    fun useHint(type: HintType) {
        if (!hintsAvailableForCurrentRun) return
        if (userAnswer.value != null) return
        val state = appState.value as? AppState.Quiz ?: return
        val inv = hintInventory.value
        if (inv.countFor(type) <= 0) return
        when (type) {
            HintType.FIFTY_FIFTY -> applyFiftyFifty(state.currentQuestion)
            HintType.REVEAL_LETTER -> applyRevealLetter(state.currentQuestion)
            HintType.SKIP -> applySkip()
        }
        persistHintInventory(inv.withDecrement(type))
    }

    private fun applyFiftyFifty(question: QuizQuestion) {
        if (questionHintState.value.fiftyFiftyUsed) return
        val correct = question.correctAnswer.titleRu
        val wrong = question.options.filter { it != correct }.shuffled().take(2).toSet()
        questionHintState.value =
            questionHintState.value.copy(
                eliminatedOptions = questionHintState.value.eliminatedOptions + wrong,
                fiftyFiftyUsed = true,
            )
    }

    private fun applyRevealLetter(question: QuizQuestion) {
        if (questionHintState.value.revealUsed) return
        val first = question.correctAnswer.titleRu.firstOrNull { !it.isWhitespace() }?.uppercase() ?: return
        questionHintState.value =
            questionHintState.value.copy(
                revealedFirstLetter = first,
                revealUsed = true,
            )
    }

    private fun applySkip() {
        skipRequested = true
        stopTimer()
        moveToNextQuestion()
    }

    fun requestRewardedAd(type: HintType) {
        if (pendingRewardedAd.value != null) return
        if (!hintsAvailableForCurrentRun) return
        pendingRewardedAd.value = type
        stopTimer()
    }

    fun completeRewardedAd() {
        val type = pendingRewardedAd.value ?: return
        pendingRewardedAd.value = null
        persistHintInventory(hintInventory.value.withIncrement(type))
        useHint(type)
        // SKIP advances to a new question, which restarts the timer via the next
        // question's AudioPlayer. The others stay on the current question, so resume here.
        if (type != HintType.SKIP && appState.value is AppState.Quiz && userAnswer.value == null) {
            startTimer()
        }
    }

    fun cancelRewardedAd() {
        pendingRewardedAd.value = null
        if (appState.value is AppState.Quiz && userAnswer.value == null) {
            startTimer()
        }
    }

    private fun persistHintInventory(next: HintInventory) {
        hintInventory.value = next
        viewModelScope.launch {
            userPrefs.setHintsFiftyFifty(next.fiftyFifty)
            userPrefs.setHintsReveal(next.revealLetter)
            userPrefs.setHintsSkip(next.skip)
        }
    }

    fun completeOnboarding() {
        needsOnboarding.value = false
        viewModelScope.launch { userPrefs.setTutorialCompleted(true) }
    }

    fun openSettings() {
        appState.value = AppState.Settings
    }

    fun openShop() {
        appState.value = AppState.Shop
    }

    /** Spend coins on a hint. No-op if the player can't afford it. */
    fun buyHintWithCoins(type: HintType) {
        val price = type.coinPrice
        if (coins.intValue < price) return
        val newTotal = coins.intValue - price
        coins.intValue = newTotal
        viewModelScope.launch { userPrefs.setCoins(newTotal) }
        persistHintInventory(hintInventory.value.withIncrement(type))
    }

    fun setSoundEnabled(value: Boolean) {
        soundEnabled.value = value
        viewModelScope.launch { userPrefs.setSoundEnabled(value) }
    }

    fun setReduceMotion(value: Boolean) {
        a11y.value = a11y.value.copy(reduceMotion = value)
        viewModelScope.launch { userPrefs.setReduceMotion(value) }
    }

    fun setColorBlindSafe(value: Boolean) {
        a11y.value = a11y.value.copy(colorBlindSafe = value)
        viewModelScope.launch { userPrefs.setColorBlindSafe(value) }
    }

    fun setLargerText(value: Boolean) {
        a11y.value = a11y.value.copy(largerText = value)
        viewModelScope.launch { userPrefs.setLargerText(value) }
    }

    fun setDyslexiaFont(value: Boolean) {
        a11y.value = a11y.value.copy(dyslexiaFont = value)
        viewModelScope.launch { userPrefs.setDyslexiaFont(value) }
    }

    fun resetHighScore() {
        repository.clearHighScore()
        _highScore.intValue = 0
    }

    fun replayTutorial() {
        viewModelScope.launch { userPrefs.setTutorialCompleted(false) }
        needsOnboarding.value = true
        appState.value = AppState.Menu
    }

    fun openDaily() {
        viewModelScope.launch {
            refreshDailyState()
            appState.value = AppState.Daily
        }
    }

    fun openProfile() {
        viewModelScope.launch {
            refreshProfileState()
            appState.value = AppState.Profile
        }
    }

    fun backToMenu() {
        appState.value = AppState.Menu
        viewModelScope.launch { refreshMenuStats() }
    }

    fun openDuelSetup() {
        isDuelRun = false
        duelListenerJob?.cancel()
        duelListenerJob = null
        duelState.value = DuelState()
        appState.value = AppState.DuelSetup
    }

    /** Host: allocate a unique code and open the lobby. Stays on setup with an error if offline. */
    fun createDuel() {
        duelState.value = duelState.value.copy(phase = DuelPhase.Connecting, errorMessage = null)
        viewModelScope.launch {
            val name = userPrefs.userName.first().ifBlank { "Игрок" }
            when (val res = duel.create(name)) {
                is DuelCreateResult.Ok -> {
                    duelState.value =
                        DuelState(
                            phase = DuelPhase.Lobby,
                            code = res.code,
                            role = DuelRole.HOST,
                            seed = res.seed,
                            myName = name,
                        )
                    observeDuel(res.code)
                    appState.value = AppState.DuelHandoff
                }

                DuelCreateResult.Offline ->
                    duelState.value =
                        duelState.value.copy(
                            phase = DuelPhase.Error,
                            errorMessage = "Нет соединения. Попробуйте позже.",
                        )
            }
        }
    }

    /** Guest: claim the code's open slot and open the lobby. */
    fun joinDuel(rawCode: String) {
        val code = rawCode.trim().uppercase()
        if (code.length != 6) {
            duelState.value =
                duelState.value.copy(phase = DuelPhase.Error, errorMessage = "Код из 6 символов.")
            return
        }
        duelState.value = duelState.value.copy(phase = DuelPhase.Connecting, errorMessage = null)
        viewModelScope.launch {
            val name = userPrefs.userName.first().ifBlank { "Игрок" }
            when (val res = duel.join(code, name)) {
                is DuelJoinResult.Ok -> {
                    duelState.value =
                        DuelState(
                            phase = DuelPhase.Lobby,
                            code = code,
                            role = DuelRole.GUEST,
                            seed = res.seed,
                            myName = name,
                            opponentName = res.hostName,
                        )
                    observeDuel(code)
                    appState.value = AppState.DuelHandoff
                }

                DuelJoinResult.NotFound ->
                    duelState.value =
                        duelState.value.copy(
                            phase = DuelPhase.Error,
                            errorMessage = "Игра с таким кодом не найдена.",
                        )

                DuelJoinResult.Full ->
                    duelState.value =
                        duelState.value.copy(
                            phase = DuelPhase.Error,
                            errorMessage = "В этой игре уже два игрока.",
                        )

                DuelJoinResult.Offline ->
                    duelState.value =
                        duelState.value.copy(
                            phase = DuelPhase.Error,
                            errorMessage = "Нет соединения. Попробуйте позже.",
                        )
            }
        }
    }

    private fun observeDuel(code: String) {
        duelListenerJob?.cancel()
        duelListenerJob =
            viewModelScope.launch {
                duel.listen(code).collect { snap ->
                    if (snap == null) return@collect
                    val s = duelState.value
                    val isHost = s.role == DuelRole.HOST
                    duelState.value =
                        s.copy(
                            opponentName =
                                (if (isHost) snap.guestName else snap.hostName) ?: s.opponentName,
                            opponentScore = if (isHost) snap.guestScore else snap.hostScore,
                            myScore = (if (isHost) snap.hostScore else snap.guestScore) ?: s.myScore,
                        )
                }
            }
    }

    /** From the lobby: load the shared seed-deterministic track and start this device's round. */
    fun startDuelRound() {
        val s = duelState.value
        if (s.code.isBlank()) {
            appState.value = AppState.Menu
            return
        }
        isDuelRun = true
        isDailyRun = false
        activeMode.value = GameMode.NORMAL
        score.intValue = 0
        userAnswer.value = null
        questionHintState.value = QuestionHintState()
        skipRequested = false
        appState.value = AppState.Loading
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                quizQuestions =
                    repository.getSeededQuestions(s.seed, DuelState.DUEL_QUESTIONS, 7.5f)
            }
            if (quizQuestions.isEmpty()) {
                appState.value = AppState.DuelHandoff
                return@launch
            }
            totalQuestionsInRun.intValue = quizQuestions.size
            runStartElapsedMs = SystemClock.elapsedRealtime()
            appState.value =
                AppState.Quiz(
                    currentQuestion = quizQuestions[0],
                    currentQuestionIndex = 0,
                )
        }
    }

    fun exitDuel() {
        isDuelRun = false
        duelListenerJob?.cancel()
        duelListenerJob = null
        duelState.value = DuelState()
        score.intValue = 0
        userAnswer.value = null
        quizQuestions = emptyList()
        appState.value = AppState.Menu
    }

    private suspend fun refreshDailyState() {
        val today = LocalDate.now(ZoneId.systemDefault())
        val epochDay = today.toEpochDay()
        val attempt =
            withContext(Dispatchers.IO) { dailyAttemptDao.forDay(epochDay) }
        val yesterday =
            withContext(Dispatchers.IO) { dailyAttemptDao.forDay(epochDay - 1) }
        val streak = userPrefs.currentStreakDays.first()
        val locale = Locale.forLanguageTag("ru")
        val dateLabel =
            today.format(DateTimeFormatter.ofPattern("d MMMM yyyy · EEEE", locale))
        dailyState.value =
            DailyState(
                epochDay = epochDay,
                today = dateLabel,
                streakDays = streak,
                attempt =
                    attempt?.let {
                        DailyAttemptSummary(
                            score = it.score,
                            correct = it.correct,
                            durationMs = it.durationMs,
                        )
                    },
                previousTrackTitle = yesterday?.trackTitle,
            )
        refreshDailyLiveStats()
    }

    /** Pull today's cross-player stats (best-effort) into [dailyState]. Safe offline. */
    private suspend fun refreshDailyLiveStats() {
        val epochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val myScore = withContext(Dispatchers.IO) { dailyAttemptDao.forDay(epochDay) }?.score
        val stats = dailyStats.loadStats(epochDay, myScore)
        if (stats != null) {
            dailyState.value = dailyState.value.copy(liveStats = stats)
        }
    }

    private suspend fun refreshProfileState() {
        val totalRuns = withContext(Dispatchers.IO) { runHistoryDao.totalRuns() }
        val recent = withContext(Dispatchers.IO) { runHistoryDao.recent(10) }
        val streak = userPrefs.currentStreakDays.first()
        val name = userPrefs.userName.first()
        val high = repository.getHighScore()
        val totalScore = withContext(Dispatchers.IO) { runHistoryDao.totalScore() ?: 0 }
        val finishedShit = withContext(Dispatchers.IO) { runHistoryDao.runsForMode(GameMode.SHIT.name) > 0 }
        val anyLightning = recent.any { it.score >= 200 }
        val accuracy =
            if (totalRuns == 0) {
                0
            } else {
                ((totalScore.toLong() * 100L) / (totalRuns.toLong() * MAX_SCORE_PER_RUN)).toInt().coerceIn(0, 100)
            }

        val xp = totalScore
        val level = (xp / XP_PER_LEVEL).coerceAtLeast(0) + 1
        val xpForNext = XP_PER_LEVEL
        val xpInLevel = xp % XP_PER_LEVEL

        val unlocked =
            Achievements.evaluate(
                UserStats(
                    totalGames = totalRuns,
                    highScore = high,
                    currentStreakDays = streak,
                    finishedConnoisseur = finishedShit,
                    anyLightningRun = anyLightning,
                ),
            )

        profileState.value =
            ProfileState(
                userName = name,
                totalGames = totalRuns,
                currentStreakDays = streak,
                accuracyPct = accuracy,
                highScore = high,
                xp = xpInLevel,
                level = level,
                xpForNextLevel = xpForNext,
                recentGames =
                    recent.map { row ->
                        RecentGame(
                            mode = runCatching { GameMode.valueOf(row.mode) }.getOrNull(),
                            score = row.score,
                            createdAt = row.createdAt,
                        )
                    },
                unlockedBadges = unlocked,
            )
    }

    fun startQuiz(gameMode: GameMode) {
        tries += 1
        activeMode.value = gameMode
        runStartElapsedMs = SystemClock.elapsedRealtime()
        isDailyRun = false
        // Per-run reset — necessary for the "Play again" flow which calls startQuiz directly
        // from ResultScreen without going through resetQuiz. Idempotent for the menu flow
        // (resetQuiz already cleared these on the way out).
        score.intValue = 0
        userAnswer.value = null
        isNewRecord.value = false
        recordModeJustSet.value = null
        questionHintState.value = QuestionHintState()
        skipRequested = false
        loadQuizQuestions(gameMode)
    }

    fun startDailyRun() {
        viewModelScope.launch {
            val attempt =
                withContext(Dispatchers.IO) {
                    dailyAttemptDao.forDay(LocalDate.now(ZoneId.systemDefault()).toEpochDay())
                }
            if (attempt != null) return@launch
            tries += 1
            isDailyRun = true
            questionHintState.value = QuestionHintState()
            skipRequested = false
            activeMode.value = GameMode.NORMAL
            runStartElapsedMs = SystemClock.elapsedRealtime()
            appState.value = AppState.Loading
            val question =
                withContext(Dispatchers.IO) {
                    repository.getDailyQuestion(LocalDate.now(ZoneId.systemDefault()).toEpochDay())
                }
            if (question == null) {
                appState.value = AppState.Daily
                return@launch
            }
            quizQuestions = listOf(question)
            totalQuestionsInRun.intValue = 1
            appState.value =
                AppState.Quiz(currentQuestion = question, currentQuestionIndex = 0)
        }
    }

    private fun loadQuizQuestions(gameMode: GameMode) {
        appState.value = AppState.Loading
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                quizQuestions =
                    when (gameMode) {
                        GameMode.EASY -> repository.getRandomizedQuestionsGt(30, 8.3f)
                        GameMode.NORMAL -> repository.getRandomizedQuestionsGt(30, 7.5f)
                        GameMode.RANDOM -> repository.getRandomizedQuestions(30)
                        GameMode.SHIT -> repository.getRandomizedQuestionsLte(30, 6f)
                    }
            }
            totalQuestionsInRun.intValue = quizQuestions.size.coerceAtLeast(1)
            appState.value =
                AppState.Quiz(
                    currentQuestion = quizQuestions[0],
                    currentQuestionIndex = 0,
                )
        }
    }

    fun startTimer() {
        timerJob?.cancel()

        timeRemaining.longValue = maxTimePerQuestion
        isTimerRunning.value = true

        val startTime = SystemClock.elapsedRealtime()

        timerJob =
            viewModelScope.launch {
                while (timeRemaining.longValue > 0L) {
                    val elapsed = SystemClock.elapsedRealtime() - startTime
                    timeRemaining.longValue = (maxTimePerQuestion - elapsed).coerceAtLeast(0L)
                    delay(16L)
                }
                isTimerRunning.value = false
                onTimeUp()
            }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false
    }

    private fun onTimeUp() {
        userAnswer.value = null
        finishRun()
    }

    fun setPosterEnabled(enabled: Boolean) {
        _isPosterEnabled.value = enabled
        viewModelScope.launch { userPrefs.setPosterEnabled(enabled) }
    }

    fun submitAnswer(selectedAnswer: String) {
        stopTimer()
        userAnswer.value = selectedAnswer
        val state = appState.value
        if (state !is AppState.Quiz) {
            appState.value = AppState.Menu
            return
        }
        if (selectedAnswer == state.currentQuestion.correctAnswer.titleRu) {
            val basePoints = (timeRemaining.longValue / 1000L).toInt()
            val multiplier = if (isDailyRun) 2 else 1
            score.intValue += basePoints * multiplier
        }
    }

    fun moveToNextQuestion() {
        val state = appState.value
        if (state !is AppState.Quiz) {
            appState.value = AppState.Menu
            return
        }
        val skipping = skipRequested
        skipRequested = false
        val correct = userAnswer.value == state.currentQuestion.correctAnswer.titleRu
        val advance = skipping || correct
        if (!advance) {
            finishRun()
            return
        }
        if (state.currentQuestionIndex < quizQuestions.size - 1) {
            appState.value =
                AppState.Quiz(
                    currentQuestion = quizQuestions[state.currentQuestionIndex + 1],
                    currentQuestionIndex = state.currentQuestionIndex + 1,
                )
            userAnswer.value = null
            questionHintState.value = QuestionHintState()
        } else {
            finishRun()
        }
    }

    private fun finishRun() {
        val finalScore = score.intValue
        val mode = activeMode.value
        val durationMs = SystemClock.elapsedRealtime() - runStartElapsedMs
        if (isDuelRun) {
            finishDuelTurn(finalScore)
            return
        }
        val previousBest = repository.getHighScore()
        isNewRecord.value = finalScore > previousBest
        recordModeJustSet.value =
            if (!isDailyRun && !isDuelRun && mode != null && finalScore > (bestScoreByMode.value[mode] ?: 0)) {
                mode
            } else {
                recordModeJustSet.value
            }
        val currentQuestion =
            (appState.value as? AppState.Quiz)?.currentQuestion
                ?: quizQuestions.firstOrNull()
        val isDaily = isDailyRun
        resultWasDaily.value = isDaily
        val correct =
            currentQuestion != null && userAnswer.value == currentQuestion.correctAnswer.titleRu

        val answeredIndex = (appState.value as? AppState.Quiz)?.currentQuestionIndex ?: 0
        val total = totalQuestionsInRun.intValue
        lastRunCorrect.intValue =
            (if (correct) answeredIndex + 1 else answeredIndex).coerceIn(0, total)
        lastRunTotal.intValue = total

        if (mode != null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    runHistoryDao.insert(
                        RunHistoryEntity(
                            mode = if (isDaily) "DAILY" else mode.name,
                            score = finalScore,
                            durationMs = durationMs,
                            dateEpochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay(),
                        ),
                    )
                    if (isDaily && currentQuestion != null) {
                        dailyAttemptDao.insert(
                            DailyAttemptEntity(
                                epochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay(),
                                score = finalScore,
                                durationMs = durationMs,
                                correct = correct,
                                trackTitle = currentQuestion.correctAnswer.titleRu,
                            ),
                        )
                    }
                }
                val streakBonus = updateStreak()
                val earned = finalScore / COINS_PER_SCORE + streakBonus
                if (earned > 0) {
                    val newTotal = userPrefs.coins.first() + earned
                    userPrefs.setCoins(newTotal)
                    coins.intValue = newTotal
                }
                lastRunCoins.intValue = earned
                if (!isDaily) {
                    leaderboard.submitScore(
                        name = userPrefs.userName.first(),
                        score = finalScore,
                        modeLabel = mode.shortLabel,
                    )
                } else {
                    dailyStats.submitAttempt(
                        epochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay(),
                        score = finalScore,
                        correct = correct,
                    )
                }
            }
        }

        appState.value = AppState.Result
    }

    /** Advances the daily streak; returns the streak-milestone coin bonus (0 unless a new
     *  7-day multiple was just reached today). */
    private suspend fun updateStreak(): Int {
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val lastPlayed = userPrefs.lastPlayedEpochDay.first()
        val current = userPrefs.currentStreakDays.first()
        val advanced = lastPlayed != today
        val next =
            when {
                lastPlayed == today -> current
                lastPlayed == today - 1 -> current + 1
                else -> 1
            }
        userPrefs.setStreak(next, today)
        return if (advanced && next > 0 && next % STREAK_BONUS_EVERY == 0) STREAK_BONUS_COINS else 0
    }

    /** Quit mid-game from the immersive quiz: forfeit — no run recorded, no high score written. */
    fun abortQuiz() {
        stopTimer()
        isDailyRun = false
        isDuelRun = false
        score.intValue = 0
        userAnswer.value = null
        isNewRecord.value = false
        questionHintState.value = QuestionHintState()
        skipRequested = false
        quizQuestions = emptyList()
        appState.value = AppState.Menu
        viewModelScope.launch { refreshMenuStats() }
    }

    fun resetQuiz() {
        repository.setHighScore(score.intValue)
        _highScore.intValue = repository.getHighScore()
        appState.value = AppState.Menu
        viewModelScope.launch { refreshMenuStats() }
        userAnswer.value = null
        score.intValue = 0
        isNewRecord.value = false
        isDailyRun = false
        questionHintState.value = QuestionHintState()
        skipRequested = false
    }

    private fun finishDuelTurn(finalScore: Int) {
        val state = duelState.value
        duelState.value = state.copy(phase = DuelPhase.Finished, myScore = finalScore)
        isDuelRun = false
        appState.value = AppState.DuelResult
        if (state.code.isNotBlank()) {
            viewModelScope.launch {
                duel.submitScore(
                    code = state.code,
                    isHost = state.role == DuelRole.HOST,
                    score = finalScore,
                )
            }
        }
    }

    companion object {
        private const val MAX_SCORE_PER_RUN = 300
        private const val XP_PER_LEVEL = 1000
        private const val COINS_PER_SCORE = 5
        private const val STREAK_BONUS_COINS = 50
        private const val STREAK_BONUS_EVERY = 7
    }
}
