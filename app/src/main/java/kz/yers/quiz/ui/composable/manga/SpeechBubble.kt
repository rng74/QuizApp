package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizStrokes

@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = Color.White,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier =
            modifier
                .padding(end = 3.dp, bottom = 14.dp)
                .drawBehind {
                    // Soft offset shadow (drawn first → behind everything).
                    drawRoundRect(
                        color = QuizColors.ink,
                        topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                    )
                    val tailSize = 18.dp.toPx()
                    val left = 24.dp.toPx()
                    val bottom = size.height
                    val tailPath =
                        Path().apply {
                            moveTo(left + tailSize / 2f, bottom - 1f)
                            lineTo(left + tailSize, bottom + tailSize / 2f)
                            lineTo(left + tailSize / 2f, bottom + tailSize)
                            lineTo(left, bottom + tailSize / 2f)
                            close()
                        }
                    drawPath(tailPath, color = background)
                    drawPath(
                        tailPath,
                        color = QuizColors.ink,
                        style = Stroke(width = QuizStrokes.panel.toPx()),
                    )
                }
                .clip(shape)
                .background(background)
                .border(QuizStrokes.panel, QuizColors.ink, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            color = QuizColors.ink,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}
