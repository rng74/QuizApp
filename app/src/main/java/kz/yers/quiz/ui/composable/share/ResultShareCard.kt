package kz.yers.quiz.ui.composable.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaChip
import kz.yers.quiz.ui.composable.manga.MangaChipVariant
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.composable.manga.SpeedLines
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

private val GlyphStyle =
    TextStyle(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle =
            LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
    )

private const val BASE_SIZE_DP = 360f

/**
 * Square share card. Internal padding/font sizes scale linearly with [size] from a 360dp design
 * baseline. Layout uses [Arrangement.SpaceBetween] so the three sections (header, score, footer)
 * always fit inside the square — no overflow, no clipping.
 */
@Composable
fun ResultShareCard(
    score: Int,
    isNewRecord: Boolean,
    modeTint: Color,
    modeDisplayName: String?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val scale = size.value / BASE_SIZE_DP
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(
        modifier =
            modifier
                .size(size)
                .clip(shape)
                .background(QuizColors.paper)
                .border(QuizStrokes.hero, QuizColors.ink, shape),
    ) {
        SpeedLines(
            modifier = Modifier.fillMaxSize(),
            color = modeTint,
            opacity = 0.10f,
            angleDegrees = 78f,
            spacingPx = 28f * scale,
            strokeWidthPx = 1.4f * scale,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = (18f * scale).dp, vertical = (20f * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header section.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ImpactText(
                    text = "АНИМЕ КВИЗ!",
                    style =
                        TextStyle(
                            fontFamily = BangersFamily,
                            fontSize = (32f * scale).sp,
                            letterSpacing = (1f * scale).sp,
                        ),
                    tintColor = modeTint,
                )
                Spacer(Modifier.height((4f * scale).dp))
                Text(
                    text = "Угадай аниме по саундтреку",
                    fontFamily = RussoOneFamily,
                    fontSize = (10f * scale).sp,
                    letterSpacing = (1f * scale).sp,
                    color = QuizColors.ink.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
                if (isNewRecord) {
                    Spacer(Modifier.height((10f * scale).dp))
                    MangaPanel(
                        modifier = Modifier.fillMaxWidth(),
                        background = QuizColors.streakFire,
                        shadowOffset = QuizShadows.medium,
                        contentPadding = (8f * scale).dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size((16f * scale).dp),
                            )
                            Spacer(Modifier.size((6f * scale).dp))
                            Text(
                                text = "НОВЫЙ РЕКОРД",
                                color = Color.White,
                                fontFamily = RussoOneFamily,
                                fontSize = (12f * scale).sp,
                                letterSpacing = (1.5f * scale).sp,
                            )
                        }
                    }
                }
            }

            // Score section (middle).
            MangaPanel(
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = QuizShadows.medium,
                contentPadding = (12f * scale).dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "ВАШ РЕЗУЛЬТАТ",
                        fontFamily = RussoOneFamily,
                        fontSize = (10f * scale).sp,
                        letterSpacing = (1.5f * scale).sp,
                        color = QuizColors.ink.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height((2f * scale).dp))
                    Text(
                        text = score.toString(),
                        fontFamily = BangersFamily,
                        fontSize = (64f * scale).sp,
                        color = QuizColors.accentBlue,
                        fontWeight = FontWeight.Normal,
                        style = GlyphStyle,
                    )
                }
            }

            // Footer section.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (modeDisplayName != null) {
                    MangaChip(
                        label = modeDisplayName,
                        variant =
                            when {
                                modeTint == QuizColors.modeShit -> MangaChipVariant.Red
                                modeTint == QuizColors.modeRandom -> MangaChipVariant.Ink
                                else -> MangaChipVariant.Tint
                            },
                    )
                    Spacer(Modifier.height((6f * scale).dp))
                }
                Text(
                    text = "kz.yers.quiz",
                    fontFamily = RussoOneFamily,
                    fontSize = (9f * scale).sp,
                    letterSpacing = (1.5f * scale).sp,
                    color = QuizColors.ink.copy(alpha = 0.5f),
                )
            }
        }
    }
}
