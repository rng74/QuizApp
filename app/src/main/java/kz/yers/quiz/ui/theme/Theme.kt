package kz.yers.quiz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import kz.yers.quiz.model.A11yState
import kz.yers.quiz.model.LocalA11y

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
    a11y: A11yState = A11yState(),
    content: @Composable () -> Unit,
) {
    val typography =
        remember(a11y.largerText, a11y.dyslexiaFont) {
            buildQuizTypography(
                bodyTextScale = if (a11y.largerText) 1.2f else 1f,
                bodyFamily = if (a11y.dyslexiaFont) DyslexiaFriendlyFamily else FontFamily.Default,
            )
        }
    CompositionLocalProvider(
        LocalModeTint provides modeTint,
        LocalA11y provides a11y,
    ) {
        MaterialTheme(
            colorScheme = QuizAppColorScheme,
            typography = typography,
            content = content,
        )
    }
}

/**
 * Resolve the colour for a "correct answer" surface, swapping to blue when colour-blind safe is on.
 * Always pair with a redundant icon (`✓` / `✕`) so the affordance survives both palettes.
 */
@Composable
fun successColor(): Color =
    if (LocalA11y.current.colorBlindSafe) QuizColors.accentBlue else QuizColors.success

/** Resolve the colour for a "wrong answer" surface, swapping to orange when colour-blind safe is on. */
@Composable
fun errorColor(): Color =
    if (LocalA11y.current.colorBlindSafe) QuizColors.tint else QuizColors.error
