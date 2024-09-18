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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.model.QuizQuestion
import kz.yers.quiz.repo.AnimeRepository

class QuizAppViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    var quizQuestions: List<QuizQuestion> = emptyList()
        private set

    val currentQuestionIndex = mutableIntStateOf(0)

    private val _highScore = mutableIntStateOf(0)
    val highScore: State<Int> = _highScore

    val score = mutableIntStateOf(0)

    val isQuizFinished = mutableStateOf(false)

    val userAnswer = mutableStateOf<String?>(null)

    val timeRemaining = mutableLongStateOf(10_000L)

    val maxTimePerQuestion = 10_000L

    private var timerJob: Job? = null

    private var isTimerRunning = mutableStateOf(false)

    val isLoading = mutableStateOf(false)

    val showMenu = mutableStateOf(true)

    private var selectedGameMode = mutableStateOf(GameMode.EASY)

    private val _isPosterEnabled = mutableStateOf(true)
    val isPosterEnabled: State<Boolean> = _isPosterEnabled

    init {
        _highScore.intValue = repository.getHighScore()
    }

    fun startQuiz(gameMode: GameMode) {
        selectedGameMode.value = gameMode
        showMenu.value = false
        loadQuizQuestions(gameMode)
    }

    private fun loadQuizQuestions(gameMode: GameMode) {
        viewModelScope.launch {
            isLoading.value = true
            withContext(Dispatchers.IO) {
                quizQuestions = when (gameMode) {
                    GameMode.EASY -> {
                        repository.getRandomizedQuestionsGt(30, 8.3f)
                    }

                    GameMode.NORMAL -> {
                        repository.getRandomizedQuestionsGt(30, 7.5f)
                    }

                    GameMode.RANDOM -> {
                        repository.getRandomizedQuestions(30)
                    }

                    GameMode.SHIT -> {
                        repository.getRandomizedQuestionsLte(30, 6f)
                    }
                }
            }
            isLoading.value = false
        }
    }

    fun startTimer() {
        timerJob?.cancel()

        timeRemaining.longValue = maxTimePerQuestion
        isTimerRunning.value = true

        val startTime = SystemClock.elapsedRealtime()

        timerJob = viewModelScope.launch {
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
        isQuizFinished.value = true
    }

    fun setPosterEnabled(enabled: Boolean) {
        _isPosterEnabled.value = enabled
    }

    fun submitAnswer(selectedAnswer: String) {
        stopTimer()
        userAnswer.value = selectedAnswer
        val currentQuestion = quizQuestions[currentQuestionIndex.intValue]
        if (selectedAnswer == currentQuestion.correctAnswer.titleRu) {
            val points = (timeRemaining.longValue / 1000L).toInt()
            score.intValue += points
        }
    }

    fun moveToNextQuestion() {
        val currentQuestion = quizQuestions[currentQuestionIndex.intValue]
        if (userAnswer.value != currentQuestion.correctAnswer.titleRu) {
            isQuizFinished.value = true
        } else if (currentQuestionIndex.intValue < quizQuestions.size - 1) {
            currentQuestionIndex.intValue += 1
            userAnswer.value = null
        } else {
            isQuizFinished.value = true
        }
    }

    fun resetQuiz() {
        repository.setHighScore(score.intValue)
        _highScore.intValue = repository.getHighScore()
        showMenu.value = true
        currentQuestionIndex.intValue = 0
        isQuizFinished.value = false
        userAnswer.value = null
        score.intValue = 0
    }
}