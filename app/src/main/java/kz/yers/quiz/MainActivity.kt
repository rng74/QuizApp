package kz.yers.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        setContent {
            QuizApp(viewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
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
