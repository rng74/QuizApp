package kz.yers.quiz.ui.composable.hints

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.R
import kz.yers.quiz.model.HintType
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

@Composable
fun HintBar(
    fiftyFiftyCount: Int,
    revealCount: Int,
    skipCount: Int,
    fiftyFiftyDisabled: Boolean,
    revealDisabled: Boolean,
    onUseHint: (HintType) -> Unit,
    onRequestAd: (HintType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HintCell(
            label = stringResource(R.string.hint_fifty_fifty),
            count = fiftyFiftyCount,
            tint = QuizColors.accentBlue,
            disabled = fiftyFiftyDisabled,
            onUse = { onUseHint(HintType.FIFTY_FIFTY) },
            onRequestAd = { onRequestAd(HintType.FIFTY_FIFTY) },
            modifier = Modifier.weight(1f),
        )
        HintCell(
            label = stringResource(R.string.hint_reveal_letter),
            count = revealCount,
            tint = QuizColors.hintPurple,
            disabled = revealDisabled,
            onUse = { onUseHint(HintType.REVEAL_LETTER) },
            onRequestAd = { onRequestAd(HintType.REVEAL_LETTER) },
            modifier = Modifier.weight(1f),
        )
        HintCell(
            label = stringResource(R.string.hint_skip),
            count = skipCount,
            tint = QuizColors.streakFire,
            disabled = false,
            onUse = { onUseHint(HintType.SKIP) },
            onRequestAd = { onRequestAd(HintType.SKIP) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HintCell(
    label: String,
    count: Int,
    tint: Color,
    disabled: Boolean,
    onUse: () -> Unit,
    onRequestAd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasCharges = count > 0 && !disabled
    val shape = RoundedCornerShape(QuizRadii.button)
    val backgroundColor = if (hasCharges) tint.copy(alpha = 0.18f) else Color.White
    val borderColor = if (hasCharges) tint else QuizColors.ink.copy(alpha = 0.5f)
    val borderWidth = if (hasCharges) QuizStrokes.panel else QuizStrokes.regular
    val contentAlpha = if (disabled) 0.4f else 1f

    Box(
        modifier =
            modifier
                .padding(end = QuizShadows.small, bottom = QuizShadows.small)
                .clip(shape)
                .background(backgroundColor)
                .border(borderWidth, borderColor, shape)
                .clickable(enabled = !disabled) {
                    if (count > 0) onUse() else onRequestAd()
                }.padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontFamily = RussoOneFamily,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                color = QuizColors.ink.copy(alpha = contentAlpha),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            if (count > 0) {
                Text(
                    text = stringResource(R.string.hint_count, count),
                    fontFamily = RussoOneFamily,
                    fontSize = 11.sp,
                    color = QuizColors.ink.copy(alpha = 0.7f * contentAlpha),
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .height(10.dp)
                                .width(10.dp)
                                .clip(RoundedCornerShape(QuizRadii.pill))
                                .background(tint),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.hint_get_more),
                        fontFamily = RussoOneFamily,
                        fontSize = 11.sp,
                        color = tint,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
