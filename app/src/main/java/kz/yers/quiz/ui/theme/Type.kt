package kz.yers.quiz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kz.yers.quiz.R
import kz.yers.quiz.model.LocalA11y

private val provider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

private val bangersFont = GoogleFont("Bangers")
private val russoOneFont = GoogleFont("Russo One")
private val lexendFont = GoogleFont("Lexend")

val BangersFamily =
    FontFamily(
        Font(googleFont = bangersFont, fontProvider = provider, weight = FontWeight.Normal),
    )

val RussoOneFamily =
    FontFamily(
        Font(googleFont = russoOneFont, fontProvider = provider, weight = FontWeight.Normal),
    )

/**
 * Lexend — Google Font designed for reading-friendliness; standing in for OpenDyslexic which is
 * not on the Google Fonts CDN. Toggled via `A11yState.dyslexiaFont`.
 */
val DyslexiaFriendlyFamily =
    FontFamily(
        Font(googleFont = lexendFont, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = lexendFont, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = lexendFont, fontProvider = provider, weight = FontWeight.Bold),
    )

private fun TextUnit.scaled(factor: Float): TextUnit = if (this.isSpecified) (this.value * factor).sp else this

private val TextUnit.isSpecified: Boolean
    get() = this != TextUnit.Unspecified

/**
 * Build a Typography instance applying:
 *   - bodyTextScale multiplier (1.0 / 1.15 / 1.3) to body + label sizes (display/headline stay
 *     constant — the manga display layer does not benefit from upsizing)
 *   - bodyFamily swap when the user prefers a dyslexia-friendly body font
 */
fun buildQuizTypography(
    bodyTextScale: Float = 1f,
    bodyFamily: FontFamily = FontFamily.Default,
): Typography {
    fun body(
        size: Int,
        line: Int,
        letter: Float,
        weight: FontWeight = FontWeight.Normal,
    ) = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = weight,
        fontSize = (size * bodyTextScale).sp,
        lineHeight = (line * bodyTextScale).sp,
        letterSpacing = letter.sp,
    )

    return Typography(
        // Bangers — display / impact (NOT scaled)
        displayLarge = TextStyle(BangersFamily, 48, 48, 1f),
        displayMedium = TextStyle(BangersFamily, 36, 36, 1f),
        displaySmall = TextStyle(BangersFamily, 28, 32, 1f),
        // Russo One — sections (NOT scaled)
        headlineLarge = TextStyle(RussoOneFamily, 24, 28, 1f),
        headlineMedium = TextStyle(RussoOneFamily, 20, 24, 1f),
        headlineSmall = TextStyle(RussoOneFamily, 16, 20, 0.5f),
        titleLarge = TextStyle(RussoOneFamily, 16, 20, 0.5f),
        // Body — scaled + optionally Lexend
        bodyLarge = body(16, 24, 0.5f),
        bodyMedium = body(14, 20, 0.25f),
        labelLarge = body(14, 18, 0.5f, FontWeight.Bold),
        labelMedium = body(11, 14, 1f, FontWeight.Bold),
    )
}

private fun TextStyle(
    family: FontFamily,
    size: Int,
    line: Int,
    letter: Float,
): TextStyle =
    TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = letter.sp,
    )

val QuizTypography: Typography = buildQuizTypography()

/** A11y-aware body font family — swaps to Lexend when the dyslexia toggle is on. */
@Composable
@ReadOnlyComposable
fun bodyFontFamily(): FontFamily =
    if (LocalA11y.current.dyslexiaFont) DyslexiaFriendlyFamily else FontFamily.Default

/** A11y-aware body scale — 1.2x when the larger-text toggle is on. */
@Composable
@ReadOnlyComposable
fun bodyScale(): Float = if (LocalA11y.current.largerText) 1.2f else 1f
