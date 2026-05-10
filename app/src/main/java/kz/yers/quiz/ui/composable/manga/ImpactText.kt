package kz.yers.quiz.ui.composable.manga

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import kz.yers.quiz.ui.theme.LocalModeTint
import kz.yers.quiz.ui.theme.QuizColors

/**
 * Bangers display text with the manga "triple-shadow" effect:
 * tint shadow at +3/+3 px, then ink shadow at +6/+6 px, then the ink-coloured glyph on top.
 *
 * The offset is in dp-equivalent units (3.dp.roundToPx() at typical density). For headlines we
 * just offset by integer px values that read well at 1x; pixel-perfect parity with the CSS
 * `text-shadow` is intentionally relaxed.
 */
@Composable
fun ImpactText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = QuizColors.ink,
    tintColor: Color? = null,
) {
    val tint = tintColor ?: LocalModeTint.current
    Box(modifier = modifier) {
        // Ink shadow — drawn first, deepest.
        Text(
            text = text,
            style = style,
            color = QuizColors.ink,
            modifier = Modifier.offsetPx(6, 6),
        )
        // Tint shadow.
        Text(
            text = text,
            style = style,
            color = tint,
            modifier = Modifier.offsetPx(3, 3),
        )
        // Front glyph.
        Text(
            text = text,
            style = style,
            color = color,
        )
    }
}

private fun Modifier.offsetPx(
    x: Int,
    y: Int,
) = this.offset { IntOffset(x, y) }
