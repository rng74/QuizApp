package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.ui.theme.LocalModeTint
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes

enum class MangaButtonVariant { Tint, Ink, Ghost }

/**
 * Offset-shadow press button. Press-state shifts the surface (2,2)dp and shrinks the shadow.
 */
@Composable
fun MangaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MangaButtonVariant = MangaButtonVariant.Tint,
    enabled: Boolean = true,
    minHeight: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp),
    cornerRadius: Dp = QuizRadii.button,
    leadingIcon: (@Composable () -> Unit)? = null,
    contentArrangement: Arrangement.Horizontal = Arrangement.Center,
    label: String,
) {
    val tint = LocalModeTint.current
    val (background, contentColor, shadowColor) =
        when (variant) {
            MangaButtonVariant.Tint -> Triple(tint, QuizColors.ink, QuizColors.ink)
            MangaButtonVariant.Ink -> Triple(QuizColors.ink, Color.White, tint)
            MangaButtonVariant.Ghost -> Triple(Color.White, QuizColors.ink, QuizColors.ink)
        }

    val press = rememberMangaPressState(enabled)

    val alpha = if (enabled) 1f else 0.4f
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier =
            modifier
                .padding(end = QuizShadows.medium, bottom = QuizShadows.medium),
    ) {
        Box(
            modifier =
                Modifier
                    .mangaPressShadow(
                        state = press,
                        restingShadow = QuizShadows.medium,
                        shadowColor = shadowColor,
                        cornerRadius = cornerRadius,
                        enabled = enabled,
                    )
                    .clip(shape)
                    .background(background.copy(alpha = if (enabled) 1f else 0.4f))
                    .border(QuizStrokes.panel, QuizColors.ink.copy(alpha = alpha), shape)
                    .mangaClickable(press, enabled = enabled, onClick = onClick)
                    .defaultMinSize(minHeight = minHeight)
                    .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor.copy(alpha = alpha)) {
                Row(
                    horizontalArrangement = contentArrangement,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Box(Modifier.padding(start = 8.dp))
                    }
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        color = contentColor.copy(alpha = alpha),
                    )
                }
            }
        }
    }
}
