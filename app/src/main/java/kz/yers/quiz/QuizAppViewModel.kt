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
import kz.yers.quiz.data.local.dao.RunHistoryDao
import kz.yers.quiz.data.local.entity.RunHistoryEntity
import kz.yers.quiz.data.prefs.UserPrefs
import kz.yers.quiz.model.AppState
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.model.QuizQuestion
import kz.yers.quiz.repo.AnimeRepository
import java.time.LocalDate
import java.time.ZoneId

class QuizAppViewModel(
    private val repository: AnimeRepository,
    private val runHistoryDao: RunHistoryDao,
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

    init {
        _highScore.intValue = repository.getHighScore()
        viewModelScope.launch {
            _isPosterEnabled.value = userPrefs.posterEnabled.first()
        }
    }

    fun startQuiz(gameMode: GameMode) {
        tries += 1
        activeMode.value = gameMode
        runStartElapsedMs = SystemClock.elapsedRealtime()
        loadQuizQuestions(gameMode)
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
            val points = (timeRemaining.longValue / 1000L).toInt()
            score.intValue += points
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
        val previousBest = repository.getHighScore()
        isNewRecord.value = finalScore > previousBest

        if (mode != null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    runHistoryDao.insert(
                        RunHistoryEntity(
                            mode = mode.name,
                            score = finalScore,
                            durationMs = durationMs,
                            dateEpochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay(),
                        ),
                    )
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
    }
}
