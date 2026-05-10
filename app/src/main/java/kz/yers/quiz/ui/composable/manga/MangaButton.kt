package kz.yers.quiz.ui.composable.manga

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    val a11y = kz.yers.quiz.model.LocalA11y.current
    val (background, contentColor, shadowColor) =
        when (variant) {
            MangaButtonVariant.Tint -> Triple(tint, QuizColors.ink, QuizColors.ink)
            MangaButtonVariant.Ink -> Triple(QuizColors.ink, Color.White, tint)
            MangaButtonVariant.Ghost -> Triple(Color.White, QuizColors.ink, QuizColors.ink)
        }

    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val pressed = isPressed && enabled && !a11y.reduceMotion

    val translate by animateDpAsState(
        targetValue = if (pressed) 2.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "press-translate",
    )
    val shadowOffset by animateDpAsState(
        targetValue = if (pressed) QuizShadows.small else QuizShadows.medium,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "press-shadow",
    )

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
                    .offset(translate, translate)
                    .drawBehind {
                        if (!enabled) return@drawBehind
                        val offsetPx = shadowOffset.toPx()
                        val r = cornerRadius.toPx()
                        drawRoundRect(
                            color = shadowColor,
                            topLeft = Offset(offsetPx, offsetPx),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(r, r),
                        )
                    }
                    .clip(shape)
                    .background(background.copy(alpha = if (enabled) 1f else 0.4f))
                    .border(QuizStrokes.panel, QuizColors.ink.copy(alpha = alpha), shape)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick,
                    )
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
