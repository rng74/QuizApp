package kz.yers.quiz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kz.yers.quiz.navigation.AppNavHost
import kz.yers.quiz.ui.theme.QuizAppTheme
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.utils.SoundManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: QuizAppViewModel by viewModel()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.init(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        maybeRequestNotificationPermission()
        routeFromIntent(intent)
        setContent {
            QuizApp(viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeFromIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /** Honors a notification tap: jump to the inbox or the daily challenge. */
    private fun routeFromIntent(intent: Intent?) {
        when (intent?.getStringExtra(EXTRA_DESTINATION)) {
            DEST_NOTIFICATIONS -> viewModel.openNotifications()
            DEST_DAILY -> viewModel.openDaily()
        }
    }

    companion object {
        const val EXTRA_DESTINATION = "destination"
        const val DEST_NOTIFICATIONS = "notifications"
        const val DEST_DAILY = "daily"
    }
}

@Composable
fun QuizApp(viewModel: QuizAppViewModel) {
    val mode by viewModel.activeMode
    val a11y by viewModel.a11y
    val soundEnabled by viewModel.soundEnabled
    val tint = mode?.tint ?: QuizColors.tint
    LaunchedEffect(soundEnabled) {
        SoundManager.enabled = soundEnabled
    }
    QuizAppTheme(modeTint = tint, a11y = a11y) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(QuizColors.paper),
        ) {
            AppNavHost(viewModel = viewModel)
        }
    }
}
