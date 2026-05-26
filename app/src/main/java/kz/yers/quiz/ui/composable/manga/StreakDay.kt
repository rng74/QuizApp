package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.ui.theme.LocalModeTint
import kz.yers.quiz.ui.theme.QuizColors

enum class StreakDayState { Empty, Done, Today }

@Composable
fun StreakDay(
    label: String,
    state: StreakDayState,
    modifier: Modifier = Modifier,
) {
    val tint = LocalModeTint.current
    val (background, content, borderWidth) =
        when (state) {
            StreakDayState.Empty -> Triple(Color.White, QuizColors.mutedStroke, 2.dp)
            StreakDayState.Done -> Triple(tint, QuizColors.ink, 2.dp)
            StreakDayState.Today -> Triple(QuizColors.streakFire, Color.White, 3.dp)
        }
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(shape)
                .background(background)
                .border(borderWidth, QuizColors.ink, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            color = content,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
        )
    }
}
