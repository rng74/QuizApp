package kz.yers.quiz.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kz.yers.quiz.QuizAppViewModel
import kz.yers.quiz.model.AppState
import kz.yers.quiz.ui.composable.screen.DailyChallengeScreen
import kz.yers.quiz.ui.composable.screen.GameModeMenuScreen
import kz.yers.quiz.ui.composable.screen.LoadingScreen
import kz.yers.quiz.ui.composable.screen.OnboardingScreen
import kz.yers.quiz.ui.composable.screen.ProfileScreen
import kz.yers.quiz.ui.composable.screen.QuizScreen
import kz.yers.quiz.ui.composable.screen.ResultScreen

@Composable
fun AppNavHost(
    viewModel: QuizAppViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val appState by viewModel.appState
    val needsOnboarding by viewModel.needsOnboarding
    val onboardingResolved by viewModel.onboardingResolved

    LaunchedEffect(needsOnboarding, onboardingResolved, appState) {
        if (!onboardingResolved) return@LaunchedEffect
        val target =
            when {
                needsOnboarding -> Routes.ONBOARDING
                else ->
                    when (appState) {
                        AppState.Menu -> Routes.MENU
                        AppState.Loading -> Routes.LOADING
                        is AppState.Quiz -> Routes.QUIZ
                        AppState.Result -> Routes.RESULT
                        AppState.Daily -> Routes.DAILY
                        AppState.Profile -> Routes.PROFILE
                    }
            }
        if (navController.currentDestination?.route != target) {
            navController.navigate(target) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.MENU,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinish = viewModel::completeOnboarding)
        }
        composable(Routes.MENU) {
            val highScore by viewModel.highScore
            val isPosterEnabled by viewModel.isPosterEnabled
            GameModeMenuScreen(
                highScore = highScore,
                isPosterEnabled = isPosterEnabled,
                onPosterToggle = viewModel::setPosterEnabled,
                onGameModeSelected = viewModel::startQuiz,
                onOpenDaily = viewModel::openDaily,
                onOpenProfile = viewModel::openProfile,
            )
        }
        composable(Routes.LOADING) {
            LoadingScreen(modeTint = viewModel.activeMode.value?.tint)
        }
        composable(Routes.QUIZ) {
            val state = appState as? AppState.Quiz ?: return@composable
            val userAnswer by viewModel.userAnswer
            val timeRemaining by viewModel.timeRemaining
            val score by viewModel.score
            val isPosterEnabled by viewModel.isPosterEnabled
            QuizScreen(
                question = state.currentQuestion,
                userAnswer = userAnswer,
                timeRemaining = timeRemaining,
                maxTime = viewModel.maxTimePerQuestion,
                score = score,
                streak = state.currentQuestionIndex + 1,
                modeTint = viewModel.activeMode.value?.tint,
                isPosterEnabled = isPosterEnabled,
                onAnswerSelected = viewModel::submitAnswer,
                onNextQuestion = viewModel::moveToNextQuestion,
                onPlaybackReady = viewModel::startTimer,
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                score = viewModel.score.intValue,
                modeTint = viewModel.activeMode.value?.tint,
                isNewRecord = viewModel.isNewRecord.value,
                needToAskReview = viewModel.tries == 3,
                onReviewSuccess = {},
                onRestart = viewModel::resetQuiz,
            )
        }
        composable(Routes.DAILY) {
            val state by viewModel.dailyState
            DailyChallengeScreen(
                state = state,
                onBack = viewModel::backToMenu,
                onPlay = viewModel::startDailyRun,
            )
        }
        composable(Routes.PROFILE) {
            val state by viewModel.profileState
            ProfileScreen(
                state = state,
                onBack = viewModel::backToMenu,
            )
        }
    }
}
