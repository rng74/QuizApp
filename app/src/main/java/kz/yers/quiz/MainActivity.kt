package kz.yers.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    val tint = mode?.tint ?: QuizColors.tint
    QuizAppTheme(modeTint = tint, a11y = a11y) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(QuizColors.paper)
                        .padding(paddingValues),
            ) {
                AppNavHost(viewModel = viewModel)
            }
        }
    }
}
