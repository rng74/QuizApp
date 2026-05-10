package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizStrokes

@Composable
fun MangaTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    background: Color = QuizColors.paper,
    leading: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(background)
                .drawBehind {
                    val stroke = QuizStrokes.panel.toPx()
                    drawLine(
                        color = QuizColors.ink,
                        start = Offset(0f, size.height - stroke / 2f),
                        end = Offset(size.width, size.height - stroke / 2f),
                        strokeWidth = stroke,
                    )
                }
                .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (leading != null) leading()
        Text(
            text = title.uppercase(),
            modifier = Modifier.weight(1f),
            color = QuizColors.ink,
            style = MaterialTheme.typography.headlineMedium,
        )
        if (actions != null) actions()
    }
}
