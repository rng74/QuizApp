package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.R
import kz.yers.quiz.model.DuelState
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaIcons
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
    val both = state.bothFinished
    val winnerIndex = state.winnerIndex
    val title =
        when {
            !both -> "ГОТОВО"
            winnerIndex == 0 -> "ПОБЕДА!"
            winnerIndex == 1 -> "ПОРАЖЕНИЕ"
            else -> "НИЧЬЯ"
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                modifier = Modifier.height(180.dp).fillMaxWidth().padding(top = 16.dp),
                contentScale = ContentScale.FillWidth,
                painter = painterResource(id = R.drawable.burst),
                contentDescription = null,
            )
            ImpactText(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                tintColor = if (winnerIndex == 0 || !both) QuizColors.streakFire else QuizColors.ink,
            )
        }
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerScoreCard(
                name = "Вы",
                score = state.myScore,
                isWinner = both && winnerIndex == 0,
                accent = QuizColors.modeEasy,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            PlayerScoreCard(
                name = state.opponentName ?: "Соперник",
                score = state.opponentScore,
                isWinner = both && winnerIndex == 1,
                accent = QuizColors.modeShit,
                waiting = state.opponentScore == null,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        Spacer(Modifier.height(16.dp))
        if (!both) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = QuizColors.tint,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text =
                        if (state.opponentName == null) {
                            "Соперник ещё не присоединился"
                        } else {
                            "Ждём результат соперника…"
                        },
                    fontFamily = RussoOneFamily,
                    fontSize = 12.sp,
                    color = QuizColors.ink.copy(alpha = 0.7f),
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        MangaButton(
            label = "НОВАЯ ДУЭЛЬ",
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
    score: Int?,
    isWinner: Boolean,
    accent: Color,
    modifier: Modifier,
    waiting: Boolean = false,
) {
    MangaPanel(
        modifier = modifier,
        background = Color.White,
        shadowOffset = QuizShadows.medium,
        contentPadding = 16.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                text = if (waiting || score == null) "…" else score.toString(),
                fontFamily = BangersFamily,
                fontSize = 44.sp,
                color = if (isWinner) QuizColors.accentBlue else QuizColors.ink,
                fontWeight = FontWeight.Normal,
                style = BadgeGlyphStyle,
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.height(18.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (isWinner) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = MangaIcons.Star,
                            contentDescription = null,
                            tint = QuizColors.streakFire,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "ПОБЕДА",
                            fontFamily = RussoOneFamily,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = QuizColors.streakFire,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            style = BadgeGlyphStyle,
                        )
                    }
                }
            }
        }
    }
}
