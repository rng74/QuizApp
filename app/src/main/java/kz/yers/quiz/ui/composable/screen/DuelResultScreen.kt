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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.DuelState
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

private val BadgeGlyphStyle =
    TextStyle(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle =
            LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
    )

@Composable
fun DuelResultScreen(
    state: DuelState,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
) {
    val winnerIndex = state.winnerIndex
    val title =
        when (winnerIndex) {
            0, 1 -> "ПОБЕДА!"
            else -> "НИЧЬЯ"
        }
    val subtitle =
        winnerIndex?.let { "Победил ${state.players[it].name}" } ?: "Счёт совпал"

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ImpactText(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            tintColor = QuizColors.streakFire,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle.uppercase(),
            fontFamily = RussoOneFamily,
            fontSize = 13.sp,
            letterSpacing = 1.5.sp,
            color = QuizColors.ink.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerScoreCard(
                name = state.players[0].name,
                score = state.players[0].score,
                isWinner = winnerIndex == 0,
                accent = QuizColors.modeEasy,
                modifier = Modifier.weight(1f),
            )
            PlayerScoreCard(
                name = state.players[1].name,
                score = state.players[1].score,
                isWinner = winnerIndex == 1,
                accent = QuizColors.modeShit,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(36.dp))

        MangaButton(
            label = "ЕЩЁ РАЗ",
            variant = MangaButtonVariant.Tint,
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 56.dp,
        )
        Spacer(Modifier.height(12.dp))
        MangaButton(
            label = "В МЕНЮ",
            variant = MangaButtonVariant.Ghost,
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 48.dp,
        )
    }
}

@Composable
private fun PlayerScoreCard(
    name: String,
    score: Int,
    isWinner: Boolean,
    accent: Color,
    modifier: Modifier,
) {
    MangaPanel(
        modifier = modifier,
        background = if (isWinner) QuizColors.paper2 else Color.White,
        shadowOffset = if (isWinner) QuizShadows.large else QuizShadows.medium,
        contentPadding = 16.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accent)
                        .border(QuizStrokes.panel, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    fontFamily = BangersFamily,
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = BadgeGlyphStyle,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = name.uppercase(),
                fontFamily = RussoOneFamily,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = QuizColors.ink,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = score.toString(),
                fontFamily = BangersFamily,
                fontSize = 44.sp,
                color = if (isWinner) QuizColors.accentBlue else QuizColors.ink,
                fontWeight = FontWeight.Normal,
                style = BadgeGlyphStyle,
            )
            if (isWinner) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "★ ПОБЕДИТЕЛЬ",
                    fontFamily = RussoOneFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.streakFire,
                )
            } else {
                Spacer(Modifier.height(4.dp))
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}
