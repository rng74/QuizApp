package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes

/**
 * Manga-style surface — bordered card with an offset rectangle shadow (no Material elevation).
 *
 * The shadow is drawn behind the panel via `drawBehind` so the panel correctly wraps its content
 * while the offset shadow tracks the panel's measured bounds.
 */
@Composable
fun MangaPanel(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    shadowColor: Color = QuizColors.ink,
    shadowOffset: Dp = QuizShadows.large,
    borderColor: Color = QuizColors.ink,
    borderWidth: Dp = QuizStrokes.panel,
    cornerRadius: Dp = QuizRadii.card,
    contentPadding: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier =
            modifier
                .padding(end = shadowOffset, bottom = shadowOffset)
                .drawBehind {
                    val offsetPx = shadowOffset.toPx()
                    val cornerPx = cornerRadius.toPx()
                    drawRoundRect(
                        color = shadowColor,
                        topLeft = Offset(offsetPx, offsetPx),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                    )
                }
                .clip(shape)
                .background(background)
                .border(borderWidth, borderColor, shape)
                .padding(contentPadding),
    ) {
        content()
    }
}
