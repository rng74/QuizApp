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
import kz.yers.quiz.model.AppState
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
    val appState by viewModel.appState
    val isPosterEnabled by viewModel.isPosterEnabled

    NavHost(navController = navController, startDestination = AppState.Menu.name) {
        composable(AppState.Quiz.name) {
            val state = appState as? AppState.Quiz ?: return@composable
            val userAnswer by viewModel.userAnswer
            val timeRemaining by viewModel.timeRemaining
            val score by viewModel.score

            QuizScreen(
                question = state.currentQuestion,
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
        composable(AppState.Result.name) {
            ResultScreen(
                score = viewModel.score.intValue,
                needToAskReview = viewModel.tries == 3,
                onReviewSuccess = {
                },
                onRestart = {
                    viewModel.resetQuiz()
                }
            )
        }
        composable(AppState.Loading.name) {
            LoadingScreen()
        }
        composable(AppState.Menu.name) {
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

    when (appState) {
        AppState.Loading -> {
            navController.navigate(AppState.Loading.name) {
                popUpTo(AppState.Loading.name) { inclusive = true }
            }
        }

        AppState.Menu -> {
            navController.navigate(AppState.Menu.name) {
                popUpTo(AppState.Menu.name) { inclusive = true }
            }
        }

        is AppState.Quiz -> {
            navController.navigate(AppState.Quiz.name) {
                popUpTo(AppState.Quiz.name) { inclusive = true }
            }
        }

        AppState.Result -> {
            navController.navigate(AppState.Result.name) {
                popUpTo(AppState.Result.name) { inclusive = true }
            }
        }
    }
}
