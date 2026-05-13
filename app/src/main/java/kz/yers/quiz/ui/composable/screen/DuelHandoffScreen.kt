package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import kz.yers.quiz.ui.composable.manga.MangaChip
import kz.yers.quiz.ui.composable.manga.MangaChipVariant
import kz.yers.quiz.ui.composable.manga.MangaIcons
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.composable.manga.SpeechBubble
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
fun DuelHandoffScreen(
    state: DuelState,
    onReady: () -> Unit,
    onExit: () -> Unit,
) {
    val current = state.currentPlayer
    val isSecondTurn = state.currentPlayerIndex == 1
    val firstPlayer = state.players[0]

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (isSecondTurn) "ХОД 2 / 2" else "ХОД 1 / 2",
            fontFamily = RussoOneFamily,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = QuizColors.ink.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(8.dp))
        Box(contentAlignment = Alignment.Center) {
            Image(
                modifier = Modifier.height(180.dp).fillMaxWidth().padding(top = 16.dp),
                contentScale = ContentScale.FillWidth,
                painter = painterResource(id = R.drawable.burst),
                contentDescription = null,
            )
            ImpactText(
                text = "ПЕРЕДАЙ ТЕЛЕФОН",
                style = MaterialTheme.typography.displaySmall,
            )
        }
        Spacer(Modifier.height(24.dp))

        MangaPanel(
            modifier = Modifier.fillMaxWidth(),
            shadowOffset = QuizShadows.large,
            contentPadding = 24.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarBadge(initial = current.name.firstOrNull()?.uppercase() ?: "?")
                Spacer(Modifier.height(8.dp))
                Icon(
                    imageVector = MangaIcons.Swords,
                    contentDescription = null,
                    tint = QuizColors.ink,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "ХОДИТ",
                    fontFamily = RussoOneFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = current.name.uppercase(),
                    fontFamily = BangersFamily,
                    fontSize = 36.sp,
                    letterSpacing = 1.sp,
                    color = QuizColors.ink,
                    textAlign = TextAlign.Center,
                    style = BadgeGlyphStyle,
                )
                Spacer(Modifier.height(12.dp))
                if (isSecondTurn) {
                    MangaChip(
                        label = "${firstPlayer.name}: ${firstPlayer.score} очков",
                        variant = MangaChipVariant.Ink,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Догони результат соперника!",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = QuizColors.ink.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = "Те же ${state.totalQuestions} треков для обоих игроков.",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = QuizColors.ink.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SpeechBubble(text = "Готов? Жми «Я готов» — таймер стартует сразу.")
        Spacer(Modifier.height(32.dp))

        MangaButton(
            label = "Я ГОТОВ",
            variant = MangaButtonVariant.Tint,
            onClick = onReady,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 56.dp,
        )
        Spacer(Modifier.height(12.dp))
        MangaButton(
            label = "ВЫЙТИ",
            variant = MangaButtonVariant.Ghost,
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 48.dp,
        )
    }
}

@Composable
private fun AvatarBadge(initial: String) {
    Box(
        modifier =
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(QuizColors.tint)
                .border(QuizStrokes.panel, QuizColors.ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            fontFamily = BangersFamily,
            fontSize = 40.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = BadgeGlyphStyle,
        )
    }
}
