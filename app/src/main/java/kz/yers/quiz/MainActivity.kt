package kz.yers.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.ui.composable.screen.GameModeMenuScreen
import kz.yers.quiz.ui.composable.screen.LoadingScreen
import kz.yers.quiz.ui.composable.screen.QuizScreen
import kz.yers.quiz.ui.composable.screen.ResultScreen
import kz.yers.quiz.ui.theme.QuizAppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: QuizAppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                QuizApp(viewModel)
            }
        }
    }
}

@Composable
fun QuizApp(viewModel: QuizAppViewModel) {
    val navController = rememberNavController()
    val isQuizFinished by viewModel.isQuizFinished
    val isLoading by viewModel.isLoading
    val showMenu by viewModel.showMenu
    val isPosterEnabled by viewModel.isPosterEnabled

    NavHost(navController = navController, startDestination = "gameModeMenu") {
        composable("quiz") {
            val currentQuestionIndex by viewModel.currentQuestionIndex
            val userAnswer by viewModel.userAnswer
            val currentQuestion = viewModel.quizQuestions[currentQuestionIndex]
            val timeRemaining by viewModel.timeRemaining
            val score by viewModel.score

            QuizScreen(
                question = currentQuestion,
                userAnswer = userAnswer,
                timeRemaining = timeRemaining,
                maxTime = viewModel.maxTimePerQuestion,
                score = score,
                isPosterEnabled = isPosterEnabled,
                onAnswerSelected = { selectedAnswer ->
                    viewModel.submitAnswer(selectedAnswer)
                }, onNextQuestion = {
                    viewModel.moveToNextQuestion()
                }, onPlaybackReady = {
                    viewModel.startTimer()
                }
            )
        }
        composable("result") {
            ResultScreen(viewModel.score.intValue, onRestart = {
                viewModel.resetQuiz()
            })
        }
        composable("loading") {
            LoadingScreen()
        }
        composable("gameModeMenu") {
            val highScore by viewModel.highScore
            GameModeMenuScreen(
                highScore = highScore,
                isPosterEnabled = isPosterEnabled,
                onPosterToggle = { enabled ->
                    viewModel.setPosterEnabled(enabled)
                }, onGameModeSelected = { gameMode: GameMode ->
                    viewModel.startQuiz(gameMode)
                }
            )
        }
    }

    if (showMenu) {
        navController.navigate("gameModeMenu") {
            popUpTo("quiz") { inclusive = true }
        }
    } else if (isQuizFinished) {
        navController.navigate("result") {
            popUpTo("quiz") { inclusive = true }
        }
    } else if (isLoading) {
        navController.navigate("loading") {
            popUpTo("quiz") { inclusive = true }
        }
    } else {
        navController.navigate("quiz") {
            popUpTo("loading") { inclusive = true }
        }
    }
}
