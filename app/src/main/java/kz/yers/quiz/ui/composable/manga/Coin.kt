package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizStrokes

@Composable
fun Coin(
    modifier: Modifier = Modifier,
    diameter: Dp = 44.dp,
    glyph: String = "¥",
) {
    Box(
        modifier =
            modifier
                .size(diameter)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color(0xFFFFF8B6),
                                        Color(0xFFFFC933),
                                        Color(0xFFCC8800),
                                    ),
                                center = Offset(size.width * 0.35f, size.height * 0.30f),
                                radius = size.maxDimension * 0.7f,
                            ),
                    )
                }
                .border(QuizStrokes.panel, QuizColors.ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            color = QuizColors.ink,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
        )
    }
}
