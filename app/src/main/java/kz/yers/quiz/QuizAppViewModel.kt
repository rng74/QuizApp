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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kz.yers.quiz.data.ads.AdsManager
import kz.yers.quiz.data.analytics.Analytics
import kz.yers.quiz.data.analytics.Events
import kz.yers.quiz.data.analytics.Params
import kz.yers.quiz.data.local.dao.DailyAttemptDao
import kz.yers.quiz.data.local.dao.FriendDao
import kz.yers.quiz.data.local.dao.RunHistoryDao
import kz.yers.quiz.data.local.entity.DailyAttemptEntity
import kz.yers.quiz.data.local.entity.FriendEntity
import kz.yers.quiz.data.local.entity.NotificationEntity
import kz.yers.quiz.data.local.entity.RunHistoryEntity
import kz.yers.quiz.data.notifications.NotificationRepository
import kz.yers.quiz.data.notifications.NotificationType
import kz.yers.quiz.data.prefs.UserPrefs
import kz.yers.quiz.data.remote.DailyStatsRepository
import kz.yers.quiz.data.remote.DuelCreateResult
import kz.yers.quiz.data.remote.DuelJoinResult
import kz.yers.quiz.data.remote.DuelRepository
import kz.yers.quiz.data.remote.FriendLookup
import kz.yers.quiz.data.remote.FriendsRepository
import kz.yers.quiz.data.remote.LeaderboardRepository
import kz.yers.quiz.model.A11yState
import kz.yers.quiz.model.Achievements
import kz.yers.quiz.model.AddFriendResult
import kz.yers.quiz.model.AppState
import kz.yers.quiz.model.DailyAttemptSummary
import kz.yers.quiz.model.DailyState
import kz.yers.quiz.model.DuelPhase
import kz.yers.quiz.model.DuelRole
import kz.yers.quiz.model.DuelState
import kz.yers.quiz.model.FriendCard
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
    private val notifications: NotificationRepository,
    private val friendDao: FriendDao,
    private val friends: FriendsRepository,
    private val analytics: Analytics,
    private val ads: AdsManager,
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

    // Identity of the question whose countdown has already been started (see onPlaybackReady).
    private var timerArmedFor: QuizQuestion? = null

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

    // Serializes every read-modify-write on the coin balance. Without this, a hint
    // purchase (buyHintWithCoins) can interleave with the run-end coin award in
    // finishRun() — both read the same `current`, both write back, and one update
    // gets dropped. All coin mutations MUST go through mutateCoins().
    private val coinsMutex = Mutex()
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

    // Lives: a miss (wrong or timeout) costs one; the run ends at zero. The daily plays all of
    // its tracks regardless, so it has no lives.
    val lives = mutableIntStateOf(MAX_LIVES)
    val livesEnabled: Boolean get() = !isDailyRun

    /** "Out of lives — watch an ad for one more?" is on screen (once per solo run). */
    val reviveOffered = mutableStateOf(false)
    val reviveAdPending = mutableStateOf(false)
    private var revivedThisRun = false
    private var runCorrect = 0

    val notificationsList = mutableStateOf<List<NotificationEntity>>(emptyList())
    val unreadCount = mutableIntStateOf(0)
    private var duelResultNotified = false

    val friendsList = mutableStateOf<List<FriendCard>>(emptyList())
    val myFriendCode = mutableStateOf<String?>(null)
    val friendsLoading = mutableStateOf(false)
    val addFriendStatus = mutableStateOf<AddFriendResult?>(null)
    private val friendScores = mutableMapOf<String, Int>()

    init {
        _highScore.intValue = repository.getHighScore()
        // Parse the track catalog while the player is on the home screen, so the first
        // "Играть" doesn't sit on "Загрузка…" for the whole 4.7 MB parse.
        // A failure here is retried by the quiz start's own load.
        viewModelScope.launch { runCatching { repository.preload() } }
        viewModelScope.launch {
            notifications.observe().collect { notificationsList.value = it }
        }
        viewModelScope.launch {
            notifications.unreadCount().collect { unreadCount.intValue = it }
        }
        viewModelScope.launch {
            friendDao.observeAll().collect { list ->
                friendsList.value = list.map { FriendCard(it.uid, it.name, friendScores[it.uid]) }
            }
        }
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

    /** Each mode has its own board, so an Easy run never outranks a Hardcore one. */
    val leaderboardMode = mutableStateOf(GameMode.NORMAL)

    fun openLeaderboard() {
        appState.value = AppState.Leaderboard
        activeMode.value?.let { leaderboardMode.value = it }
        loadLeaderboard()
    }

    fun selectLeaderboardMode(mode: GameMode) {
        if (mode == leaderboardMode.value && leaderboardState.value !is LeaderboardUiState.Offline) return
        leaderboardMode.value = mode
        loadLeaderboard()
    }

    private var leaderboardJob: Job? = null

    fun loadLeaderboard() {
        leaderboardState.value = LeaderboardUiState.Loading
        val mode = leaderboardMode.value
        leaderboardJob?.cancel()
        leaderboardJob =
            viewModelScope.launch {
                leaderboardState.value = leaderboard.load(mode)
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
        analytics.log(
            Events.HINT_USED,
            Params.HINT_TYPE to type.name,
            Params.QUESTION_INDEX to state.currentQuestionIndex,
            Params.MODE to (activeMode.value?.name ?: "UNKNOWN"),
        )
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
        analytics.log(Events.HINT_AD_REQUESTED, Params.HINT_TYPE to type.name)
    }

    fun completeRewardedAd() {
        val type = pendingRewardedAd.value ?: return
        pendingRewardedAd.value = null
        persistHintInventory(hintInventory.value.withIncrement(type))
        analytics.log(Events.HINT_AD_REWARDED, Params.HINT_TYPE to type.name)
        useHint(type)
        // SKIP advances to a new question, which restarts the timer via the next
        // question's AudioPlayer. The others stay on the current question, so resume here.
        if (type != HintType.SKIP && appState.value is AppState.Quiz && userAnswer.value == null) {
            resumeTimer()
        }
    }

    fun cancelRewardedAd() {
        val type = pendingRewardedAd.value
        pendingRewardedAd.value = null
        if (type != null) {
            analytics.log(Events.HINT_AD_CANCELED, Params.HINT_TYPE to type.name)
        }
        if (appState.value is AppState.Quiz && userAnswer.value == null) {
            resumeTimer()
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
        analytics.log(Events.ONBOARDING_COMPLETED)
    }

    fun openSettings() {
        appState.value = AppState.Settings
    }

    fun openShop() {
        appState.value = AppState.Shop
    }

    fun openNotifications() {
        appState.value = AppState.Notifications
        analytics.log(
            Events.NOTIF_INBOX_OPENED,
            Params.UNREAD_COUNT to unreadCount.intValue,
        )
        viewModelScope.launch { notifications.markAllRead() }
    }

    fun openFriends() {
        appState.value = AppState.Friends
        addFriendStatus.value = null
        analytics.log(Events.FRIENDS_OPENED, Params.UNREAD_COUNT to friendsList.value.size)
        refreshFriends()
    }

    /** Pull this device's friend code + each friend's current best score (best-effort). */
    fun refreshFriends() {
        if (friendsLoading.value) return
        friendsLoading.value = true
        viewModelScope.launch {
            myFriendCode.value = friends.myUid()
            val entities = withContext(Dispatchers.IO) { friendDao.all() }
            entities.forEach { e ->
                friends.fetch(e.uid)?.let { friendScores[e.uid] = it.bestScore }
            }
            friendsList.value =
                entities.map { FriendCard(it.uid, it.name, friendScores[it.uid]) }
            friendsLoading.value = false
        }
    }

    /** Add a friend by their code (= their anon uid). Surfaces the outcome in [addFriendStatus]. */
    fun addFriend(rawCode: String) {
        val code = rawCode.trim()
        if (code.isEmpty()) return
        viewModelScope.launch {
            val mine = friends.myUid()
            if (code == mine) {
                addFriendStatus.value = AddFriendResult.Self
                analytics.log(Events.FRIEND_ADD_RESULT, Params.RESULT to "self")
                return@launch
            }
            when (val res = friends.lookup(code)) {
                is FriendLookup.Found -> {
                    friendScores[res.profile.uid] = res.profile.bestScore
                    withContext(Dispatchers.IO) {
                        friendDao.upsert(
                            FriendEntity(uid = res.profile.uid, name = res.profile.name),
                        )
                    }
                    addFriendStatus.value = AddFriendResult.Ok
                    analytics.log(Events.FRIEND_ADD_RESULT, Params.RESULT to "ok")
                }

                FriendLookup.NotFound -> {
                    addFriendStatus.value = AddFriendResult.NotFound
                    analytics.log(Events.FRIEND_ADD_RESULT, Params.RESULT to "not_found")
                }
                FriendLookup.Offline -> {
                    addFriendStatus.value = AddFriendResult.Offline
                    analytics.log(Events.FRIEND_ADD_RESULT, Params.RESULT to "offline")
                }
            }
        }
    }

    fun removeFriend(uid: String) {
        friendScores.remove(uid)
        viewModelScope.launch { withContext(Dispatchers.IO) { friendDao.delete(uid) } }
        analytics.log(Events.FRIEND_REMOVED)
    }

    fun clearAddFriendStatus() {
        addFriendStatus.value = null
    }

    /** Spend coins on a hint. No-op if the player can't afford it. */
    fun buyHintWithCoins(type: HintType) {
        val price = type.coinPrice
        viewModelScope.launch {
            val ok = mutateCoins { it - price }
            if (ok) {
                persistHintInventory(hintInventory.value.withIncrement(type))
                analytics.log(
                    Events.HINT_BOUGHT_COINS,
                    Params.HINT_TYPE to type.name,
                    Params.PRICE to price,
                )
                analytics.log(
                    Events.COINS_SPENT,
                    Params.COINS_DELTA to price,
                    Params.COINS_SINK to "hint",
                )
            }
        }
    }

    /**
     * Single atomic point of mutation for the coin balance. Reads the canonical
     * value from [UserPrefs] (not the in-memory mirror — that can lag a write),
     * applies [delta], persists it, and refreshes the mirror — all under
     * [coinsMutex] so concurrent callers serialize.
     *
     * Returns false (and writes nothing) when [delta] would push the balance
     * below zero — callers can use this to surface "insufficient funds".
     */
    private suspend fun mutateCoins(delta: (Int) -> Int): Boolean =
        coinsMutex.withLock {
            val current = userPrefs.coins.first()
            val next = delta(current)
            if (next < 0) return@withLock false
            userPrefs.setCoins(next)
            coins.intValue = next
            true
        }

    fun setSoundEnabled(value: Boolean) {
        soundEnabled.value = value
        viewModelScope.launch { userPrefs.setSoundEnabled(value) }
        logSettingToggle("sound", value)
    }

    fun setReduceMotion(value: Boolean) {
        a11y.value = a11y.value.copy(reduceMotion = value)
        viewModelScope.launch { userPrefs.setReduceMotion(value) }
        logSettingToggle("reduce_motion", value)
    }

    fun setColorBlindSafe(value: Boolean) {
        a11y.value = a11y.value.copy(colorBlindSafe = value)
        viewModelScope.launch { userPrefs.setColorBlindSafe(value) }
        logSettingToggle("color_blind_safe", value)
    }

    fun setLargerText(value: Boolean) {
        a11y.value = a11y.value.copy(largerText = value)
        viewModelScope.launch { userPrefs.setLargerText(value) }
        logSettingToggle("larger_text", value)
    }

    fun setDyslexiaFont(value: Boolean) {
        a11y.value = a11y.value.copy(dyslexiaFont = value)
        viewModelScope.launch { userPrefs.setDyslexiaFont(value) }
        logSettingToggle("dyslexia_font", value)
    }

    private fun logSettingToggle(
        key: String,
        value: Boolean,
    ) {
        analytics.log(Events.SETTING_TOGGLED, Params.KEY to key, Params.VALUE to value)
    }

    fun resetHighScore() {
        repository.clearHighScore()
        _highScore.intValue = 0
        analytics.log(Events.HIGH_SCORE_RESET)
    }

    fun replayTutorial() {
        viewModelScope.launch { userPrefs.setTutorialCompleted(false) }
        needsOnboarding.value = true
        appState.value = AppState.Menu
        analytics.log(Events.TUTORIAL_REPLAYED)
    }

    fun openDaily() {
        viewModelScope.launch {
            refreshDailyState()
            appState.value = AppState.Daily
            analytics.log(
                Events.DAILY_OPENED,
                Params.STREAK_DAYS to streakDays.intValue,
                "already_played" to (dailyState.value.attempt != null),
            )
        }
    }

    fun openProfile() {
        viewModelScope.launch {
            refreshProfileState()
            appState.value = AppState.Profile
        }
    }

    fun backToMenu() {
        // Any non-duel return to menu must also drop the Firestore snapshot listener;
        // otherwise the duel observer keeps streaming after the screen is gone and
        // burns quota / leaks across the next duel session. See stopDuelObserver().
        stopDuelObserver()
        appState.value = AppState.Menu
        viewModelScope.launch { refreshMenuStats() }
    }

    fun openDuelSetup() {
        isDuelRun = false
        duelResultNotified = false
        stopDuelObserver()
        duelState.value = DuelState()
        appState.value = AppState.DuelSetup
        analytics.log(Events.DUEL_EXITED, Params.DUEL_PHASE to "setup_opened")
    }

    /** Single point for tearing down the Firestore duel snapshot listener. */
    private fun stopDuelObserver() {
        duelListenerJob?.cancel()
        duelListenerJob = null
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
                    maybeNotifyDuelResult()
                }
            }
    }

    /** Once both duel scores are in, drop a single inbox + tray row with the outcome. */
    private fun maybeNotifyDuelResult() {
        val s = duelState.value
        if (duelResultNotified || !s.bothFinished) return
        duelResultNotified = true
        val title =
            when (s.winnerIndex) {
                0 -> "Победа в дуэли! 🥇"
                1 -> "Поражение в дуэли"
                else -> "Ничья в дуэли"
            }
        viewModelScope.launch {
            notifications.notify(
                type = NotificationType.DUEL_RESULT,
                title = title,
                body = "Ты: ${s.myScore} · ${s.opponentName ?: "Соперник"}: ${s.opponentScore}",
            )
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
        resetRunState()
        appState.value = AppState.Loading
        analytics.log(
            Events.DUEL_ROUND_STARTED,
            Params.DUEL_ROLE to (s.role?.name ?: "UNKNOWN"),
        )
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
        analytics.log(
            Events.DUEL_EXITED,
            Params.DUEL_PHASE to duelState.value.phase.name,
            Params.DUEL_ROLE to (duelState.value.role?.name ?: "UNKNOWN"),
        )
        isDuelRun = false
        stopDuelObserver()
        duelState.value = DuelState()
        resetRunState()
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
                            correctCount = it.correctCount,
                            totalTracks = it.totalTracks,
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
        val totalCorrect = withContext(Dispatchers.IO) { runHistoryDao.totalCorrect() ?: 0 }
        val totalAnswered = withContext(Dispatchers.IO) { runHistoryDao.totalAnswered() ?: 0 }
        val accuracy =
            if (totalAnswered == 0) 0 else (totalCorrect * 100 / totalAnswered).coerceIn(0, 100)

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
        resetRunState()
        isNewRecord.value = false
        recordModeJustSet.value = null
        analytics.log(Events.QUIZ_STARTED, Params.MODE to gameMode.name)
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
            isNewRecord.value = false
            resetRunState()
            activeMode.value = GameMode.NORMAL
            runStartElapsedMs = SystemClock.elapsedRealtime()
            appState.value = AppState.Loading
            val questions =
                withContext(Dispatchers.IO) {
                    repository.getDailyQuestions(LocalDate.now(ZoneId.systemDefault()).toEpochDay())
                }
            if (questions.isEmpty()) {
                appState.value = AppState.Daily
                return@launch
            }
            quizQuestions = questions
            totalQuestionsInRun.intValue = questions.size
            analytics.log(
                Events.DAILY_STARTED,
                Params.STREAK_DAYS to streakDays.intValue,
            )
            appState.value =
                AppState.Quiz(currentQuestion = questions[0], currentQuestionIndex = 0)
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

    /**
     * Arms the countdown for the current question — at most once per question. ExoPlayer
     * reports STATE_READY again after every rebuffer, and a playback error may arrive before
     * READY ever does; both paths call this, and neither may hand the player a fresh 10 s.
     */
    fun onPlaybackReady() {
        val question = (appState.value as? AppState.Quiz)?.currentQuestion ?: return
        if (timerArmedFor === question || userAnswer.value != null) return
        timerArmedFor = question
        runTimer(maxTimePerQuestion)
    }

    /** Continues an armed-but-paused countdown (after a rewarded ad) from where it stopped. */
    private fun resumeTimer() {
        val question = (appState.value as? AppState.Quiz)?.currentQuestion ?: return
        if (timerArmedFor !== question) return
        runTimer(timeRemaining.longValue)
    }

    private fun runTimer(fromMs: Long) {
        timerJob?.cancel()

        timeRemaining.longValue = fromMs
        isTimerRunning.value = true

        val startTime = SystemClock.elapsedRealtime()

        timerJob =
            viewModelScope.launch {
                while (timeRemaining.longValue > 0L) {
                    val elapsed = SystemClock.elapsedRealtime() - startTime
                    timeRemaining.longValue = (fromMs - elapsed).coerceAtLeast(0L)
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
        // Don't end the run instantly: park on the question so the correct answer is revealed,
        // and let the "results" button (moveToNextQuestion) finish the run as a miss.
        if (appState.value !is AppState.Quiz || userAnswer.value != null) return
        userAnswer.value = TIMED_OUT
        if (livesEnabled) lives.intValue = (lives.intValue - 1).coerceAtLeast(0)
        analytics.log(
            Events.QUIZ_ANSWERED,
            Params.MODE to mode(),
            Params.QUESTION_INDEX to (appState.value as AppState.Quiz).currentQuestionIndex,
            Params.CORRECT to false,
            Params.TIME_REMAINING_MS to 0L,
        )
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
        val isCorrect = selectedAnswer == state.currentQuestion.correctAnswer.titleRu
        val timeRemainingMs = timeRemaining.longValue
        if (isCorrect) {
            val basePoints = (timeRemainingMs / 1000L).toInt()
            val multiplier = if (isDailyRun) 2 else 1
            score.intValue += basePoints * multiplier
            runCorrect += 1
        } else if (livesEnabled) {
            lives.intValue = (lives.intValue - 1).coerceAtLeast(0)
        }
        analytics.log(
            Events.QUIZ_ANSWERED,
            Params.MODE to mode(),
            Params.QUESTION_INDEX to state.currentQuestionIndex,
            Params.CORRECT to isCorrect,
            Params.TIME_REMAINING_MS to timeRemainingMs,
        )
    }

    private fun mode(): String =
        when {
            isDailyRun -> "DAILY"
            isDuelRun -> "DUEL"
            else -> activeMode.value?.name ?: "UNKNOWN"
        }

    fun moveToNextQuestion() {
        val state = appState.value
        if (state !is AppState.Quiz) {
            appState.value = AppState.Menu
            return
        }
        val skipping = skipRequested
        skipRequested = false
        val missed = !skipping && userAnswer.value != state.currentQuestion.correctAnswer.titleRu
        if (missed && livesEnabled && lives.intValue <= 0) {
            val hasNextQuestion = state.currentQuestionIndex < quizQuestions.size - 1
            if (!isDuelRun && !revivedThisRun && hasNextQuestion) {
                reviveOffered.value = true
                analytics.log(Events.REVIVE_OFFERED, Params.MODE to mode())
            } else {
                finishRun()
            }
            return
        }
        advanceOrFinish(state)
    }

    /** Out of lives: the player chose to watch a rewarded ad for one more life. */
    fun acceptRevive() {
        if (!reviveOffered.value) return
        reviveOffered.value = false
        reviveAdPending.value = true
    }

    fun declineRevive() {
        if (!reviveOffered.value) return
        reviveOffered.value = false
        finishRun()
    }

    /** Result of the revive rewarded ad: +1 life and continue, or end the run. */
    fun onReviveAdResult(earned: Boolean) {
        if (!reviveAdPending.value) return
        reviveAdPending.value = false
        val state = appState.value as? AppState.Quiz
        if (!earned || state == null) {
            finishRun()
            return
        }
        revivedThisRun = true
        lives.intValue = 1
        analytics.log(Events.REVIVE_USED, Params.MODE to mode())
        advanceOrFinish(state)
    }

    private fun advanceOrFinish(state: AppState.Quiz) {
        if (state.currentQuestionIndex < quizQuestions.size - 1) {
            appState.value =
                AppState.Quiz(
                    currentQuestion = quizQuestions[state.currentQuestionIndex + 1],
                    currentQuestionIndex = state.currentQuestionIndex + 1,
                )
            userAnswer.value = null
            questionHintState.value = QuestionHintState()
            timeRemaining.longValue = maxTimePerQuestion
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
        // The high score is a solo-mode record; daily runs (2× points) don't count toward it.
        isNewRecord.value = !isDailyRun && finalScore > previousBest
        // Persist here, not on the way back to the menu: "Play again" goes straight to
        // startQuiz() and would otherwise drop the record.
        if (isNewRecord.value) {
            repository.setHighScore(finalScore)
            _highScore.intValue = finalScore
        }
        val answeredIndex = (appState.value as? AppState.Quiz)?.currentQuestionIndex ?: 0
        val total = totalQuestionsInRun.intValue
        val outOfLives = livesEnabled && lives.intValue <= 0
        val completed = !outOfLives && answeredIndex >= total - 1
        val endedVia = if (outOfLives) "out_of_lives" else "complete"
        analytics.log(
            Events.QUIZ_FINISHED,
            Params.MODE to mode(),
            Params.SCORE to finalScore,
            Params.QUESTION_INDEX to answeredIndex,
            Params.CORRECT_COUNT to runCorrect,
            Params.TOTAL_QUESTIONS to totalQuestionsInRun.intValue,
            Params.DURATION_MS to durationMs,
            Params.ENDED_VIA to endedVia,
        )
        if (isNewRecord.value && finalScore > 0) {
            analytics.log(
                Events.NEW_HIGH_SCORE,
                Params.SCORE to finalScore,
                Params.MODE to (mode?.name ?: "UNKNOWN"),
            )
        }
        recordModeJustSet.value =
            if (!isDailyRun && !isDuelRun && mode != null && finalScore > (bestScoreByMode.value[mode] ?: 0)) {
                mode
            } else {
                recordModeJustSet.value
            }
        val isDaily = isDailyRun
        resultWasDaily.value = isDaily
        if (!isDaily) ads.onRunFinished()
        val correctCount = runCorrect
        lastRunCorrect.intValue = correctCount
        lastRunTotal.intValue = total
        // Questions the player actually faced (skips count as faced, not as correct).
        val answeredCount = (answeredIndex + 1).coerceAtMost(total)
        val dailyTitles = if (isDaily) quizQuestions.joinToString(" · ") { it.correctAnswer.titleRu } else ""
        val dailySolved = correctCount >= DAILY_SOLVED_MIN
        // Set before navigating so the result screen's first frame already shows the run's coins;
        // the streak bonus (known only after the async streak update) is added below.
        val perCorrect = if (isDaily) DAILY_COINS_PER_CORRECT else COINS_PER_CORRECT
        val completionBonus = if (!isDaily && completed) RUN_COMPLETE_BONUS else 0
        val baseCoins = correctCount * perCorrect + completionBonus
        lastRunCoins.intValue = baseCoins

        if (mode != null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    runHistoryDao.insert(
                        RunHistoryEntity(
                            mode = if (isDaily) "DAILY" else mode.name,
                            score = finalScore,
                            durationMs = durationMs,
                            dateEpochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay(),
                            correct = correctCount,
                            answered = answeredCount,
                        ),
                    )
                    if (isDaily) {
                        dailyAttemptDao.insert(
                            DailyAttemptEntity(
                                epochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay(),
                                score = finalScore,
                                durationMs = durationMs,
                                correct = dailySolved,
                                trackTitle = dailyTitles,
                                correctCount = correctCount,
                                totalTracks = total,
                            ),
                        )
                    }
                }
                val streakBonus = updateStreak()
                val earned = baseCoins + streakBonus
                if (earned > 0) {
                    mutateCoins { it + earned }
                    analytics.log(
                        Events.COINS_EARNED,
                        Params.COINS_DELTA to earned,
                        Params.COINS_SOURCE to if (streakBonus > 0) "streak_bonus" else "run",
                    )
                }
                if (streakBonus > 0) {
                    analytics.log(
                        Events.STREAK_MILESTONE,
                        Params.STREAK_DAYS to userPrefs.currentStreakDays.first(),
                    )
                }
                lastRunCoins.intValue = earned
                emitRunNotifications(
                    isNewRecord = isNewRecord.value,
                    finalScore = finalScore,
                    isDaily = isDaily,
                    streakBonusEarned = streakBonus > 0,
                )
                if (!isDaily && finalScore > 0) {
                    // Don't poison the global leaderboard with brand-new-player 0-scores —
                    // a player who never answered correctly should not appear at all.
                    leaderboard.submitScore(
                        name = userPrefs.userName.first(),
                        score = finalScore,
                        mode = mode,
                    )
                } else if (isDaily) {
                    dailyStats.submitAttempt(
                        epochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay(),
                        score = finalScore,
                        correct = dailySolved,
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

    /** Inbox + tray rows for a finished solo/daily run (record, streak milestone, daily done). */
    private suspend fun emitRunNotifications(
        isNewRecord: Boolean,
        finalScore: Int,
        isDaily: Boolean,
        streakBonusEarned: Boolean,
    ) {
        if (isNewRecord) {
            notifications.notify(
                type = NotificationType.NEW_RECORD,
                title = "Новый рекорд! 🏆",
                body = "Ты набрал $finalScore очков — это твой лучший результат.",
            )
        }
        if (streakBonusEarned) {
            val streak = userPrefs.currentStreakDays.first()
            notifications.notify(
                type = NotificationType.STREAK_MILESTONE,
                title = "Серия $streak дней! 🔥",
                body = "Бонус +$STREAK_BONUS_COINS монет за серию. Так держать!",
            )
        }
        if (isDaily) {
            notifications.notify(
                type = NotificationType.DAILY_DONE,
                title = "Дневной вызов пройден ✅",
                body = "Ты заработал $finalScore очков сегодня. Возвращайся завтра!",
                systemTray = false,
            )
        }
    }

    /** Quit mid-game from the immersive quiz: forfeit — no run recorded, no high score written. */
    fun abortQuiz() {
        val atIndex = (appState.value as? AppState.Quiz)?.currentQuestionIndex ?: 0
        analytics.log(
            Events.QUIZ_FORFEITED,
            Params.MODE to mode(),
            Params.QUESTION_INDEX to atIndex,
            Params.SCORE to score.intValue,
        )
        isDailyRun = false
        isDuelRun = false
        resetRunState()
        isNewRecord.value = false
        quizQuestions = emptyList()
        appState.value = AppState.Menu
        viewModelScope.launch { refreshMenuStats() }
    }

    fun resetQuiz() {
        appState.value = AppState.Menu
        viewModelScope.launch { refreshMenuStats() }
        resetRunState()
        isNewRecord.value = false
        isDailyRun = false
    }

    /** Clears every per-run field. Called at each run start and on every way out of a run. */
    private fun resetRunState() {
        stopTimer()
        score.intValue = 0
        userAnswer.value = null
        questionHintState.value = QuestionHintState()
        timeRemaining.longValue = maxTimePerQuestion
        skipRequested = false
        lives.intValue = MAX_LIVES
        reviveOffered.value = false
        reviveAdPending.value = false
        revivedThisRun = false
        runCorrect = 0
    }

    private fun finishDuelTurn(finalScore: Int) {
        val state = duelState.value
        duelState.value = state.copy(phase = DuelPhase.Finished, myScore = finalScore)
        isDuelRun = false
        appState.value = AppState.DuelResult
        analytics.log(
            Events.DUEL_FINISHED,
            Params.SCORE to finalScore,
            Params.DUEL_ROLE to (state.role?.name ?: "UNKNOWN"),
        )
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
        /** Sentinel [userAnswer] for a question whose timer ran out. Matches no option. */
        const val TIMED_OUT = ""
        private const val XP_PER_LEVEL = 1000
        const val MAX_LIVES = 3

        // A daily counts as "solved" (shared stats) with at least this many of its tracks right.
        const val DAILY_SOLVED_MIN = 3
        private const val COINS_PER_CORRECT = 5
        private const val DAILY_COINS_PER_CORRECT = 10
        private const val RUN_COMPLETE_BONUS = 25
        private const val STREAK_BONUS_COINS = 50
        private const val STREAK_BONUS_EVERY = 7
    }
}
