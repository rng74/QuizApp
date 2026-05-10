package kz.yers.quiz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalModeTint = staticCompositionLocalOf { QuizColors.tint }

private val QuizAppColorScheme =
    lightColorScheme(
        primary = QuizColors.tint,
        onPrimary = Color.White,
        secondary = QuizColors.accentBlue,
        onSecondary = Color.White,
        background = QuizColors.paper,
        onBackground = QuizColors.ink,
        surface = QuizColors.paper,
        onSurface = QuizColors.ink,
        surfaceVariant = QuizColors.paper2,
        onSurfaceVariant = QuizColors.ink,
        outline = QuizColors.ink,
        error = QuizColors.error,
        onError = Color.White,
    )

@Composable
fun QuizAppTheme(
    modeTint: Color = QuizColors.tint,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalModeTint provides modeTint) {
        MaterialTheme(
            colorScheme = QuizAppColorScheme,
            typography = QuizTypography,
            content = content,
        )
    }
}
