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
import kz.yers.quiz.model.A11yState
import kz.yers.quiz.model.Achievements
import kz.yers.quiz.model.AppState
import kz.yers.quiz.model.DailyAttemptSummary
import kz.yers.quiz.model.DailyState
import kz.yers.quiz.model.DuelPlayer
import kz.yers.quiz.model.DuelState
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.model.ProfileState
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
    val totalQuestionsInRun = mutableIntStateOf(30)

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
        }
    }

    fun completeOnboarding() {
        needsOnboarding.value = false
        viewModelScope.launch { userPrefs.setTutorialCompleted(true) }
    }

    fun openSettings() {
        appState.value = AppState.Settings
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
    }

    fun openDuelSetup() {
        isDuelRun = false
        duelState.value = DuelState()
        appState.value = AppState.DuelSetup
    }

    fun startDuel(
        playerOneName: String,
        playerTwoName: String,
    ) {
        val name1 = playerOneName.trim().ifBlank { "Игрок 1" }
        val name2 = playerTwoName.trim().ifBlank { "Игрок 2" }
        duelState.value =
            DuelState(
                players = listOf(DuelPlayer(name1), DuelPlayer(name2)),
                currentPlayerIndex = 0,
            )
        isDuelRun = true
        isDailyRun = false
        activeMode.value = GameMode.NORMAL
        score.intValue = 0
        userAnswer.value = null
        appState.value = AppState.Loading
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                quizQuestions = repository.getRandomizedQuestionsGt(DuelState.DUEL_QUESTIONS, 7.5f)
            }
            totalQuestionsInRun.intValue = quizQuestions.size.coerceAtLeast(1)
            appState.value = AppState.DuelHandoff
        }
    }

    fun proceedFromDuelHandoff() {
        if (!isDuelRun || quizQuestions.isEmpty()) {
            appState.value = AppState.Menu
            return
        }
        score.intValue = 0
        userAnswer.value = null
        runStartElapsedMs = SystemClock.elapsedRealtime()
        appState.value =
            AppState.Quiz(
                currentQuestion = quizQuestions[0],
                currentQuestionIndex = 0,
            )
    }

    fun exitDuel() {
        isDuelRun = false
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
        if (userAnswer.value != state.currentQuestion.correctAnswer.titleRu) {
            finishRun()
        } else if (state.currentQuestionIndex < quizQuestions.size - 1) {
            appState.value =
                AppState.Quiz(
                    currentQuestion = quizQuestions[state.currentQuestionIndex + 1],
                    currentQuestionIndex = state.currentQuestionIndex + 1,
                )
            userAnswer.value = null
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
        val currentQuestion =
            (appState.value as? AppState.Quiz)?.currentQuestion
                ?: quizQuestions.firstOrNull()
        val isDaily = isDailyRun
        val correct =
            currentQuestion != null && userAnswer.value == currentQuestion.correctAnswer.titleRu

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
                updateStreak()
            }
        }

        appState.value = AppState.Result
    }

    private suspend fun updateStreak() {
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val lastPlayed = userPrefs.lastPlayedEpochDay.first()
        val current = userPrefs.currentStreakDays.first()
        val next =
            when {
                lastPlayed == today -> current
                lastPlayed == today - 1 -> current + 1
                else -> 1
            }
        userPrefs.setStreak(next, today)
    }

    fun resetQuiz() {
        repository.setHighScore(score.intValue)
        _highScore.intValue = repository.getHighScore()
        appState.value = AppState.Menu
        userAnswer.value = null
        score.intValue = 0
        isNewRecord.value = false
        isDailyRun = false
    }

    private fun finishDuelTurn(finalScore: Int) {
        val state = duelState.value
        val index = state.currentPlayerIndex
        val answered =
            (appState.value as? AppState.Quiz)?.let { it.currentQuestionIndex + 1 }
                ?: state.totalQuestions
        val updatedPlayers =
            state.players.toMutableList().also { list ->
                list[index] = list[index].copy(score = finalScore, answeredQuestions = answered)
            }
        if (index == 0) {
            duelState.value =
                state.copy(players = updatedPlayers, currentPlayerIndex = 1)
            appState.value = AppState.DuelHandoff
        } else {
            duelState.value = state.copy(players = updatedPlayers)
            appState.value = AppState.DuelResult
        }
    }

    companion object {
        private const val MAX_SCORE_PER_RUN = 300
        private const val XP_PER_LEVEL = 1000
    }
}
