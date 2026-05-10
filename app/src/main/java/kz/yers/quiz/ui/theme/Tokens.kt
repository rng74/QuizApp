package kz.yers.quiz.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object QuizColors {
    val tint = Color(0xFFFE9400)
    val tintSoft = Color(0x1FFE9400)
    val ink = Color(0xFF0A0A0A)
    val paper = Color(0xFFFBF6EC)
    val paper2 = Color(0xFFF3EBD9)
    val streakFire = Color(0xFFFF3C2A)
    val hintPurple = Color(0xFF7B3AFF)
    val accentBlue = Color(0xFF006AFE)
    val success = Color(0xFF00D400)
    val error = Color(0xFFFE1500)

    val modeEasy = Color(0xFF2E9E5B)
    val modeNormal = tint
    val modeRandom = hintPurple
    val modeShit = Color(0xFFD12A2A)
}

object QuizStrokes {
    val thin: Dp = 1.5.dp
    val regular: Dp = 2.dp
    val panel: Dp = 2.5.dp
    val hero: Dp = 3.5.dp
}

object QuizShadows {
    val small: Dp = 2.dp
    val medium: Dp = 4.dp
    val large: Dp = 6.dp
}

object QuizRadii {
    val sharp: Dp = 0.dp
    val card: Dp = 6.dp
    val button: Dp = 8.dp
    val pill: Dp = 999.dp
}
