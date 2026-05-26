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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.LeaderboardEntry
import kz.yers.quiz.model.LeaderboardUiState
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

private val PERIODS = listOf("День", "Неделя", "Месяц", "Все время")

@Composable
fun LeaderboardScreen(
    state: LeaderboardUiState,
    onRetry: () -> Unit,
) {
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
            // Only "Все время" is backed by data for now (single best-score doc per player).
            PERIODS.forEach { label -> PeriodChip(label = label, selected = label == "Все время") }
        }

        when (state) {
            LeaderboardUiState.Loading -> LoadingPanel()
            LeaderboardUiState.Offline -> OfflinePanel(onRetry = onRetry)
            is LeaderboardUiState.Loaded -> LoadedContent(state)
        }
    }
}

@Composable
private fun LoadingPanel() {
    Box(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = QuizColors.tint, strokeWidth = 3.dp)
    }
}

@Composable
private fun OfflinePanel(onRetry: () -> Unit) {
    OffsetShadowBox(
        modifier = Modifier.fillMaxWidth(),
        background = Color.White,
        shadowOffset = QuizShadows.small,
        radius = 12.dp,
        border = QuizStrokes.panel,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Топ недоступен",
                fontFamily = RussoOneFamily,
                fontSize = 16.sp,
                color = QuizColors.ink,
            )
            Text(
                text = "Нет соединения. Проверьте интернет и попробуйте снова.",
                fontSize = 13.sp,
                color = QuizColors.ink.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
            MangaButton(
                label = "ОБНОВИТЬ",
                variant = MangaButtonVariant.Tint,
                onClick = onRetry,
            )
        }
    }
}

@Composable
private fun LoadedContent(state: LeaderboardUiState.Loaded) {
    YourRankCard(myRank = state.myRank, myScore = state.myScore, total = state.totalPlayers)
    if (state.rows.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Пока никто не сыграл. Будьте первым!",
                fontSize = 13.sp,
                color = QuizColors.ink.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            state.rows.forEach { LeaderboardRow(it) }
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
        Text(
            text = label.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = if (selected) Color.White else QuizColors.ink,
        )
    }
}

@Composable
private fun YourRankCard(
    myRank: Int?,
    myScore: Int,
    total: Int,
) {
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
                Text(
                    text = myRank?.let { "#$it" } ?: "—",
                    fontFamily = RussoOneFamily,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ваше место",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text =
                        if (myRank == null) {
                            "Сыграйте, чтобы попасть в топ"
                        } else {
                            "Счёт $myScore · игроков: $total"
                        },
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(row: LeaderboardEntry) {
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
            RankDisc(row.rank)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (row.isYou) "Вы" else row.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = QuizColors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (row.mode.isNotBlank()) {
                    Text(
                        text = row.mode.uppercase(),
                        fontFamily = RussoOneFamily,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = QuizColors.ink.copy(alpha = 0.6f),
                    )
                }
            }
            Text(
                text = row.score.toString(),
                fontFamily = RussoOneFamily,
                fontSize = 20.sp,
                color = QuizColors.tintDeep,
            )
        }
    }
}

@Composable
private fun RankDisc(rank: Int) {
    val gradient =
        when (rank) {
            1 -> Brush.linearGradient(listOf(QuizColors.gold, QuizColors.goldDeep))
            2 -> Brush.linearGradient(listOf(QuizColors.silver, QuizColors.silverDeep))
            3 -> Brush.linearGradient(listOf(QuizColors.bronze, QuizColors.bronzeDeep))
            else -> Brush.linearGradient(listOf(QuizColors.paper2, QuizColors.paper2))
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
        Text(
            text = rank.toString(),
            fontFamily = RussoOneFamily,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = if (rank <= 3) Color.White else QuizColors.ink,
        )
    }
}

/** Bordered surface with an offset rectangle ink shadow (shared manga chrome). */
@Composable
private fun OffsetShadowBox(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    shadowOffset: Dp = QuizShadows.small,
    radius: Dp = QuizRadii.button,
    border: Dp = QuizStrokes.regular,
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
