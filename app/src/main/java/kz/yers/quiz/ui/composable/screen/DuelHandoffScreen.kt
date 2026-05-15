package kz.yers.quiz.ui.composable.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.DuelRole
import kz.yers.quiz.model.DuelState
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.composable.manga.SpeechBubble
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

@Composable
fun DuelHandoffScreen(
    state: DuelState,
    onStart: () -> Unit,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    val isHost = state.role == DuelRole.HOST
    val opponentJoined = state.opponentName != null

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
            text = if (isHost) "ВЫ СОЗДАЛИ ИГРУ" else "ВЫ ВОШЛИ В ИГРУ",
            fontFamily = RussoOneFamily,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = QuizColors.ink.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(8.dp))
        ImpactText(text = "КОД ИГРЫ", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(20.dp))

        MangaPanel(
            modifier = Modifier.fillMaxWidth(),
            shadowOffset = QuizShadows.large,
            contentPadding = 24.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CodeBox(state.code)
                Spacer(Modifier.height(16.dp))
                Text(
                    text =
                        when {
                            !isHost -> "Игра: ${state.opponentName}"
                            opponentJoined -> "Соперник: ${state.opponentName}"
                            else -> "Ждём второго игрока…"
                        },
                    fontFamily = RussoOneFamily,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = QuizColors.ink.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        SpeechBubble(
            text =
                if (isHost) {
                    "Отправь код другу. Можешь играть свой раунд сразу — счёт сверится потом."
                } else {
                    "Те же ${state.totalQuestions} треков, что и у соперника. Готов?"
                },
        )
        Spacer(Modifier.height(28.dp))

        if (isHost) {
            MangaButton(
                label = "ПОДЕЛИТЬСЯ КОДОМ",
                variant = MangaButtonVariant.Ghost,
                onClick = { shareCode(context, state.code) },
                modifier = Modifier.fillMaxWidth(),
                minHeight = 52.dp,
            )
            Spacer(Modifier.height(12.dp))
        }
        MangaButton(
            label = "НАЧАТЬ РАУНД",
            variant = MangaButtonVariant.Tint,
            onClick = onStart,
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
private fun CodeBox(code: String) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier =
            Modifier
                .clip(shape)
                .background(QuizColors.ink)
                .border(QuizStrokes.panel, QuizColors.ink, shape)
                .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = code,
            fontFamily = BangersFamily,
            fontSize = 44.sp,
            letterSpacing = 10.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

private fun shareCode(
    context: Context,
    code: String,
) {
    val msg = "Сыграй со мной в АНИМЕ КВИЗ! Код дуэли: $code"
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
        ?.setPrimaryClip(ClipData.newPlainText("duel code", code))
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, msg)
        }
    context.startActivity(Intent.createChooser(send, "Поделиться кодом"))
}
