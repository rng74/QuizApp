package kz.yers.quiz.ui.composable.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

private data class LbRow(
    val rank: Int,
    val name: String,
    val meta: String,
    val score: Int,
    val medal: Medal? = null,
    val isYou: Boolean = false,
)

private enum class Medal { GOLD, SILVER, BRONZE }

private val SAMPLE_ROWS =
    listOf(
        LbRow(1, "otaku_47", "Норм", 287, Medal.GOLD),
        LbRow(2, "sakura_chan", "Изи", 264, Medal.SILVER),
        LbRow(3, "shadow_kira", "Рандом", 251, Medal.BRONZE),
        LbRow(4, "BasedSenpai", "Шарю", 239),
        LbRow(5, "mori_neko", "Норм", 218),
        LbRow(6, "bishounen", "Изи", 196),
        LbRow(23, "Вы", "Норм · это вы", 42, isYou = true),
    )

private val PERIODS = listOf("Неделя", "День", "Месяц", "Все время")

/**
 * Static stub of the global leaderboard ("Топ"). Period chips, the player's rank summary, and
 * a ranked list are hardcoded sample data — the real ranked feed is a backend concern
 * (see V2_REDESIGN_PLAN.md).
 */
@Composable
fun LeaderboardScreen() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PERIODS.forEachIndexed { index, label ->
                PeriodChip(label = label, selected = index == 0)
            }
        }

        YourRankCard()

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SAMPLE_ROWS.forEach { LeaderboardRow(it) }
        }
    }
}

@Composable
private fun PeriodChip(
    label: String,
    selected: Boolean,
) {
    val shape = RoundedCornerShape(QuizRadii.pill)
    Box(
        modifier =
            Modifier
                .clip(shape)
                .background(if (selected) QuizColors.ink else Color.White)
                .border(QuizStrokes.regular, QuizColors.ink, shape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        androidx.compose.material3.Text(
            text = label.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = if (selected) Color.White else QuizColors.ink,
        )
    }
}

@Composable
private fun YourRankCard() {
    OffsetShadowBox(
        modifier = Modifier.fillMaxWidth(),
        background = QuizColors.paper2,
        shadowOffset = QuizShadows.small,
        radius = 12.dp,
        border = QuizStrokes.panel,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(QuizColors.hintPurple)
                        .border(QuizStrokes.panel, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Text(
                    text = "#23",
                    fontFamily = RussoOneFamily,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(
                    text = "Ваше место",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(2.dp))
                androidx.compose.material3.Text(
                    text = "До топ-10: ещё 14 баллов",
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(row: LbRow) {
    OffsetShadowBox(
        modifier = Modifier.fillMaxWidth(),
        background = if (row.isYou) QuizColors.paper2 else Color.White,
        shadowOffset = QuizShadows.small,
        radius = QuizRadii.button,
        border = QuizStrokes.regular,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RankDisc(row)
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(
                    text = row.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = QuizColors.ink,
                )
                androidx.compose.material3.Text(
                    text = row.meta.uppercase(),
                    fontFamily = RussoOneFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
            }
            androidx.compose.material3.Text(
                text = row.score.toString(),
                fontFamily = RussoOneFamily,
                fontSize = 20.sp,
                color = QuizColors.tintDeep,
            )
        }
    }
}

@Composable
private fun RankDisc(row: LbRow) {
    val gradient =
        when (row.medal) {
            Medal.GOLD -> Brush.linearGradient(listOf(QuizColors.gold, QuizColors.goldDeep))
            Medal.SILVER -> Brush.linearGradient(listOf(QuizColors.silver, QuizColors.silverDeep))
            Medal.BRONZE -> Brush.linearGradient(listOf(QuizColors.bronze, QuizColors.bronzeDeep))
            null -> Brush.linearGradient(listOf(QuizColors.paper2, QuizColors.paper2))
        }
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(gradient)
                .border(QuizStrokes.regular, QuizColors.ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = row.rank.toString(),
            fontFamily = RussoOneFamily,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = if (row.medal != null) Color.White else QuizColors.ink,
        )
    }
}

/** Bordered surface with an offset rectangle ink shadow (shared manga chrome). */
@Composable
private fun OffsetShadowBox(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    shadowOffset: androidx.compose.ui.unit.Dp = QuizShadows.small,
    radius: androidx.compose.ui.unit.Dp = QuizRadii.button,
    border: androidx.compose.ui.unit.Dp = QuizStrokes.regular,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier =
            modifier
                .padding(end = shadowOffset, bottom = shadowOffset)
                .drawBehind {
                    val o = shadowOffset.toPx()
                    val r = radius.toPx()
                    drawRoundRect(
                        color = QuizColors.ink,
                        topLeft = Offset(o, o),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(r, r),
                    )
                }
                .clip(shape)
                .background(background)
                .border(border, QuizColors.ink, shape),
    ) {
        content()
    }
}
