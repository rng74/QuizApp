package kz.yers.quiz.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kz.yers.quiz.QuizAppViewModel
import kz.yers.quiz.model.AppState
import kz.yers.quiz.ui.composable.scaffold.MainScaffold
import kz.yers.quiz.ui.composable.screen.DailyChallengeScreen
import kz.yers.quiz.ui.composable.screen.DuelHandoffScreen
import kz.yers.quiz.ui.composable.screen.DuelResultScreen
import kz.yers.quiz.ui.composable.screen.DuelSetupScreen
import kz.yers.quiz.ui.composable.screen.FriendsScreen
import kz.yers.quiz.ui.composable.screen.GameModeMenuScreen
import kz.yers.quiz.ui.composable.screen.HintShopScreen
import kz.yers.quiz.ui.composable.screen.LeaderboardScreen
import kz.yers.quiz.ui.composable.screen.LoadingScreen
import kz.yers.quiz.ui.composable.screen.NotificationInboxScreen
import kz.yers.quiz.ui.composable.screen.OnboardingScreen
import kz.yers.quiz.ui.composable.screen.ProfileScreen
import kz.yers.quiz.ui.composable.screen.QuizScreen
import kz.yers.quiz.ui.composable.screen.ResultScreen
import kz.yers.quiz.ui.composable.screen.SettingsActions
import kz.yers.quiz.ui.composable.screen.SettingsScreen

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
                        AppState.Leaderboard -> Routes.LEADERBOARD
                        AppState.Settings -> Routes.SETTINGS
                        AppState.Notifications -> Routes.NOTIFICATIONS
                        AppState.Friends -> Routes.FRIENDS
                        AppState.Shop -> Routes.SHOP
                        AppState.DuelSetup -> Routes.DUEL_SETUP
                        AppState.DuelHandoff -> Routes.DUEL_HANDOFF
                        AppState.DuelResult -> Routes.DUEL_RESULT
                    }
            }
        if (navController.currentDestination?.route != target) {
            navController.navigate(target) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // System-back semantics per the navigation contract:
    //  - Home (Menu): not handled → default → exit app.
    //  - Other root tabs + Settings: → Home.
    //  - Quiz: handled inside QuizScreen (forfeit confirm).
    //  - Loading/Result/Duel transient: → sensible parent.
    BackHandler(enabled = appState != AppState.Menu && appState !is AppState.Quiz) {
        when (appState) {
            AppState.Loading -> viewModel.abortQuiz()
            AppState.Result -> viewModel.resetQuiz()
            AppState.DuelHandoff, AppState.DuelResult -> viewModel.exitDuel()
            else -> viewModel.backToMenu()
        }
    }

    val currentRoute by navController.currentBackStackEntryAsState()
    MainScaffold(
        currentRoute = currentRoute?.destination?.route,
        coins = viewModel.coins.intValue,
        notifCount = viewModel.unreadCount.intValue,
        onTab = { index ->
            when (index) {
                0 -> viewModel.backToMenu()
                1 -> viewModel.openDaily()
                2 -> viewModel.openDuelSetup()
                3 -> viewModel.openLeaderboard()
                4 -> viewModel.openProfile()
            }
        },
        onOpenSettings = viewModel::openSettings,
        onOpenShop = viewModel::openShop,
        onOpenNotifications = viewModel::openNotifications,
        onBack = viewModel::backToMenu,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.MENU,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(160)) },
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onFinish = viewModel::completeOnboarding)
            }
            composable(Routes.MENU) {
                val highScore by viewModel.highScore
                GameModeMenuScreen(
                    highScore = highScore,
                    streakDays = viewModel.streakDays.intValue,
                    bestScoreByMode = viewModel.bestScoreByMode.value,
                    recordModeJustSet = viewModel.recordModeJustSet.value,
                    dailyStats = viewModel.dailyState.value.liveStats,
                    onGameModeSelected = viewModel::startQuiz,
                    onOpenDaily = viewModel::openDaily,
                    onOpenDuel = viewModel::openDuelSetup,
                )
            }
            composable(Routes.LEADERBOARD) {
                LeaderboardScreen(
                    state = viewModel.leaderboardState.value,
                    onRetry = viewModel::loadLeaderboard,
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
                val totalQuestions by viewModel.totalQuestionsInRun
                val hintInventory by viewModel.hintInventory
                val questionHintState by viewModel.questionHintState
                val pendingAd by viewModel.pendingRewardedAd
                QuizScreen(
                    question = state.currentQuestion,
                    userAnswer = userAnswer,
                    timeRemaining = timeRemaining,
                    maxTime = viewModel.maxTimePerQuestion,
                    score = score,
                    streak = state.currentQuestionIndex + 1,
                    totalQuestions = totalQuestions,
                    modeTint = viewModel.activeMode.value?.tint,
                    isPosterEnabled = isPosterEnabled,
                    hintsAvailable = viewModel.hintsAvailableForCurrentRun,
                    hintInventory = hintInventory,
                    questionHintState = questionHintState,
                    pendingAdType = pendingAd,
                    onAnswerSelected = viewModel::submitAnswer,
                    onNextQuestion = viewModel::moveToNextQuestion,
                    onPlaybackReady = viewModel::startTimer,
                    onUseHint = viewModel::useHint,
                    onRequestAd = viewModel::requestRewardedAd,
                    onAdComplete = viewModel::completeRewardedAd,
                    onAdCancel = viewModel::cancelRewardedAd,
                    onClose = viewModel::abortQuiz,
                )
            }
            composable(Routes.RESULT) {
                ResultScreen(
                    score = viewModel.score.intValue,
                    modeTint = viewModel.activeMode.value?.tint,
                    modeDisplayName = viewModel.activeMode.value?.displayName,
                    isNewRecord = viewModel.isNewRecord.value,
                    isDaily = viewModel.resultWasDaily.value,
                    coinsEarned = viewModel.lastRunCoins.intValue,
                    correctCount = viewModel.lastRunCorrect.intValue,
                    totalQuestions = viewModel.lastRunTotal.intValue,
                    needToAskReview = viewModel.tries == 3,
                    onReviewSuccess = {},
                    onRestart = viewModel::resetQuiz,
                    onPlayAgain = {
                        val mode = viewModel.activeMode.value
                        if (mode != null) viewModel.startQuiz(mode) else viewModel.resetQuiz()
                    },
                )
            }
            composable(Routes.DAILY) {
                val state by viewModel.dailyState
                DailyChallengeScreen(
                    state = state,
                    onPlay = viewModel::startDailyRun,
                )
            }
            composable(Routes.PROFILE) {
                val state by viewModel.profileState
                ProfileScreen(state = state, onOpenFriends = viewModel::openFriends)
            }
            composable(Routes.FRIENDS) {
                FriendsScreen(
                    myCode = viewModel.myFriendCode.value,
                    friends = viewModel.friendsList.value,
                    loading = viewModel.friendsLoading.value,
                    myBestScore = viewModel.highScore.value,
                    addStatus = viewModel.addFriendStatus.value,
                    onAddFriend = viewModel::addFriend,
                    onRemoveFriend = viewModel::removeFriend,
                    onClearStatus = viewModel::clearAddFriendStatus,
                )
            }
            composable(Routes.DUEL_SETUP) {
                val state by viewModel.duelState
                DuelSetupScreen(
                    state = state,
                    onCreate = viewModel::createDuel,
                    onJoin = viewModel::joinDuel,
                    onBack = viewModel::backToMenu,
                )
            }
            composable(Routes.DUEL_HANDOFF) {
                val state by viewModel.duelState
                DuelHandoffScreen(
                    state = state,
                    onStart = viewModel::startDuelRound,
                    onExit = viewModel::exitDuel,
                )
            }
            composable(Routes.DUEL_RESULT) {
                val state by viewModel.duelState
                DuelResultScreen(
                    state = state,
                    onPlayAgain = viewModel::openDuelSetup,
                    onExit = viewModel::exitDuel,
                )
            }
            composable(Routes.SETTINGS) {
                val a11y by viewModel.a11y
                val isPosterEnabled by viewModel.isPosterEnabled
                val soundEnabled by viewModel.soundEnabled
                SettingsScreen(
                    isPosterEnabled = isPosterEnabled,
                    soundEnabled = soundEnabled,
                    a11y = a11y,
                    actions =
                        SettingsActions(
                            onPosterToggle = viewModel::setPosterEnabled,
                            onSoundToggle = viewModel::setSoundEnabled,
                            onReduceMotionToggle = viewModel::setReduceMotion,
                            onColorBlindToggle = viewModel::setColorBlindSafe,
                            onLargerTextToggle = viewModel::setLargerText,
                            onDyslexiaToggle = viewModel::setDyslexiaFont,
                            onReplayTutorial = viewModel::replayTutorial,
                            onResetHighScore = viewModel::resetHighScore,
                        ),
                )
            }
            composable(Routes.NOTIFICATIONS) {
                val ctx = androidx.compose.ui.platform.LocalContext.current
                // Android 13+ runtime permission. We probe at every screen entry
                // (cheap) so toggling system settings outside the app reflects
                // back instantly on return.
                val denied =
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            ctx,
                            android.Manifest.permission.POST_NOTIFICATIONS,
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                NotificationInboxScreen(
                    notifications = viewModel.notificationsList.value,
                    showPermissionBanner = denied,
                    onOpenSystemSettings = {
                        val intent =
                            android.content.Intent(
                                android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                            ).apply {
                                putExtra(
                                    android.provider.Settings.EXTRA_APP_PACKAGE,
                                    ctx.packageName,
                                )
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        runCatching { ctx.startActivity(intent) }
                    },
                )
            }
            composable(Routes.SHOP) {
                HintShopScreen(
                    coins = viewModel.coins.intValue,
                    inventory = viewModel.hintInventory.value,
                    pendingAdType = viewModel.pendingRewardedAd.value,
                    onBuyWithCoins = viewModel::buyHintWithCoins,
                    onWatchAd = viewModel::requestRewardedAd,
                    onAdReward = viewModel::completeRewardedAd,
                    onAdDismiss = viewModel::cancelRewardedAd,
                )
            }
        }
    }
}
