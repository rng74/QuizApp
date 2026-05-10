package kz.yers.quiz.ui.composable.manga

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.LocalA11y
import kz.yers.quiz.ui.theme.LocalModeTint
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizStrokes

@Composable
fun MangaSwitchRow(
    label: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = QuizColors.ink,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            )
            if (description != null) {
                Text(
                    text = description,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
        MangaSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun MangaSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = LocalModeTint.current
    val a11y = LocalA11y.current
    val trackShape = RoundedCornerShape(14.dp)
    val targetX = if (checked) 24.dp else 4.dp
    val knobX by animateDpAsState(
        targetValue = targetX,
        animationSpec = spring(stiffness = if (a11y.reduceMotion) Spring.StiffnessHigh * 4 else Spring.StiffnessMedium),
        label = "switch-knob",
    )
    Box(
        modifier =
            modifier
                .size(width = 48.dp, height = 28.dp)
                .clip(trackShape)
                .background(if (checked) tint else Color(0xFFDDDDDD))
                .border(QuizStrokes.regular, QuizColors.ink, trackShape)
                .clickable { onCheckedChange(!checked) },
    ) {
        Box(
            modifier =
                Modifier
                    .offset(x = knobX, y = 4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(QuizStrokes.thin, QuizColors.ink, CircleShape),
        )
    }
}
