package kz.yers.quiz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QuizAppColorScheme = lightColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    secondary = Blue700,
    // Add other colors as needed
)

@Composable
fun QuizAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QuizAppColorScheme,
        typography = Typography(
            // Customize your typography here
        ),
        content = content
    )
}