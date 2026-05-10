package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kz.yers.quiz.ui.theme.QuizColors

/**
 * Repeating dot pattern, drawn on a 6dp grid. Tile this on top of any surface that needs
 * a manga halftone fill.
 */
@Composable
fun HalftoneOverlay(
    modifier: Modifier = Modifier,
    color: Color = QuizColors.ink,
    opacity: Float = 0.35f,
    spacing: Dp = 6.dp,
    radius: Dp = 1.2.dp,
) {
    Box(
        modifier =
            modifier.drawBehind {
                val step = spacing.toPx()
                val r = radius.toPx()
                val tinted = color.copy(alpha = opacity)
                var y = 0f
                while (y < size.height) {
                    var x = 0f
                    while (x < size.width) {
                        drawCircle(color = tinted, radius = r, center = androidx.compose.ui.geometry.Offset(x, y))
                        x += step
                    }
                    y += step
                }
            },
    )
}
