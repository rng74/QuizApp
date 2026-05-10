package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kz.yers.quiz.ui.theme.QuizColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Diagonal speed-line streaks. Use as a hero overlay (Daily, Profile, Versus).
 */
@Composable
fun SpeedLines(
    modifier: Modifier = Modifier,
    color: Color = QuizColors.ink,
    angleDegrees: Float = 85f,
    opacity: Float = 0.35f,
    spacingPx: Float = 22f,
    strokeWidthPx: Float = 1f,
) {
    Box(
        modifier =
            modifier.drawBehind {
                val rad = Math.toRadians(angleDegrees.toDouble())
                val dx = cos(rad).toFloat()
                val dy = sin(rad).toFloat()
                val length = (size.width + size.height) * 1.5f
                val tinted = color.copy(alpha = opacity)
                var t = -size.height
                while (t < size.width + size.height) {
                    val start = Offset(t, -size.height)
                    val end = Offset(start.x + dx * length, start.y + dy * length)
                    drawLine(color = tinted, start = start, end = end, strokeWidth = strokeWidthPx)
                    t += spacingPx
                }
            },
    )
}
