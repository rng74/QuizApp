package kz.yers.quiz.ui.composable.manga

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kz.yers.quiz.model.LocalA11y
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows

/**
 * Shared "pushed-in" press model for manga components. The surface shifts down-right and
 * (for shadowed surfaces) the hard offset-shadow shrinks, replacing the Material ripple.
 */
class MangaPressState internal constructor(
    val interactionSource: MutableInteractionSource,
    val pressed: Boolean,
)

/**
 * Press state gated by [enabled] and the user's reduce-motion setting — when motion is
 * reduced [MangaPressState.pressed] stays `false` so animations resolve to rest (no jump).
 */
@Composable
fun rememberMangaPressState(enabled: Boolean = true): MangaPressState {
    val src = remember { MutableInteractionSource() }
    val isPressed by src.collectIsPressedAsState()
    val reduceMotion = LocalA11y.current.reduceMotion
    return MangaPressState(src, isPressed && enabled && !reduceMotion)
}

/**
 * Full effect: translate the surface in and shrink its hard offset-shadow. Place where the
 * component currently draws its shadow — before its own clip/background/border.
 */
@Composable
fun Modifier.mangaPressShadow(
    state: MangaPressState,
    restingShadow: Dp,
    shadowColor: Color = QuizColors.ink,
    cornerRadius: Dp = QuizRadii.button,
    pressedShadow: Dp = QuizShadows.small,
    enabled: Boolean = true,
): Modifier {
    val translate by animateDpAsState(
        targetValue = if (state.pressed) restingShadow - pressedShadow else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "press-translate",
    )
    val shadowOffset by animateDpAsState(
        targetValue = if (state.pressed) pressedShadow else restingShadow,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "press-shadow",
    )
    return this
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
}

/** Flat effect: subtle inward nudge only, for components with no offset-shadow. */
@Composable
fun Modifier.mangaPressNudge(
    state: MangaPressState,
    distance: Dp = 2.dp,
): Modifier {
    val translate by animateDpAsState(
        targetValue = if (state.pressed) distance else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "press-nudge",
    )
    return this.offset(translate, translate)
}

/** Ripple-less click wired to the shared press state. */
fun Modifier.mangaClickable(
    state: MangaPressState,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier =
    this.clickable(
        interactionSource = state.interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
