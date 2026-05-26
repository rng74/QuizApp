package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizStrokes

enum class MangaChipVariant { Default, Tint, Red, Blue, Ink }

@Composable
fun MangaChip(
    label: String,
    modifier: Modifier = Modifier,
    variant: MangaChipVariant = MangaChipVariant.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val tint = LocalModeTint.current
    val (background, content) =
        when (variant) {
            MangaChipVariant.Default -> Color.White to QuizColors.ink
            MangaChipVariant.Tint -> tint to QuizColors.ink
            MangaChipVariant.Red -> QuizColors.chipRedBg to Color.White
            MangaChipVariant.Blue -> QuizColors.accentBlue to Color.White
            MangaChipVariant.Ink -> QuizColors.ink to Color.White
        }
    val shape = RoundedCornerShape(QuizRadii.pill)
    Row(
        modifier =
            modifier
                .clip(shape)
                .background(background)
                .border(QuizStrokes.regular, QuizColors.ink, shape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (leadingIcon != null) leadingIcon()
        Text(
            text = label.uppercase(),
            color = content,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp,
        )
    }
}
