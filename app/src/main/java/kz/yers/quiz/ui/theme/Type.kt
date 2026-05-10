package kz.yers.quiz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import kz.yers.quiz.R

private val provider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

private val bangersFont = GoogleFont("Bangers")
private val russoOneFont = GoogleFont("Russo One")

val BangersFamily =
    FontFamily(
        Font(googleFont = bangersFont, fontProvider = provider, weight = FontWeight.Normal),
    )

val RussoOneFamily =
    FontFamily(
        Font(googleFont = russoOneFont, fontProvider = provider, weight = FontWeight.Normal),
    )

private val baseBody =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    )

val QuizTypography =
    Typography(
        // Bangers — display / impact
        displayLarge =
            TextStyle(
                fontFamily = BangersFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 48.sp,
                lineHeight = 48.sp,
                letterSpacing = 1.sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = BangersFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 36.sp,
                letterSpacing = 1.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = BangersFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                letterSpacing = 1.sp,
            ),
        // Russo One — section / app-bar titles
        headlineLarge =
            TextStyle(
                fontFamily = RussoOneFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                letterSpacing = 1.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = RussoOneFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                letterSpacing = 1.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = RussoOneFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.5.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = RussoOneFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.5.sp,
            ),
        // Roboto / system — body + buttons
        bodyLarge = baseBody,
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.5.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.sp,
            ),
    )

@Deprecated("Use QuizTypography", ReplaceWith("QuizTypography"))
val Typography: Typography = QuizTypography
