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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.DuelPhase
import kz.yers.quiz.model.DuelState
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaChip
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

@Composable
fun DuelSetupScreen(
    state: DuelState,
    onCreate: () -> Unit,
    onJoin: (String) -> Unit,
    onBack: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    val busy = state.phase == DuelPhase.Connecting

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        ImpactText(text = "ДУЭЛЬ", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Сыграй с другом по коду — у вас одинаковые треки",
            color = QuizColors.ink,
            fontFamily = RussoOneFamily,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MangaChip(label = "10 вопросов")
            MangaChip(label = "Норм >8")
            MangaChip(label = "Одни треки")
        }

        Spacer(Modifier.height(24.dp))

        MangaButton(
            label = if (busy) "СОЗДАЁМ…" else "СОЗДАТЬ ИГРУ",
            variant = MangaButtonVariant.Tint,
            onClick = { if (!busy) onCreate() },
            modifier = Modifier.fillMaxWidth(),
            minHeight = 56.dp,
        )

        Spacer(Modifier.height(20.dp))
        Text(
            text = "ИЛИ ВОЙДИ ПО КОДУ",
            fontFamily = RussoOneFamily,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = QuizColors.ink.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(12.dp))

        MangaPanel(
            modifier = Modifier.fillMaxWidth(),
            background = QuizColors.paper2,
            shadowOffset = QuizShadows.medium,
            contentPadding = 18.dp,
        ) {
            CodeField(value = code, onValueChange = { code = it })
        }

        Spacer(Modifier.height(14.dp))
        MangaButton(
            label = "ПРИСОЕДИНИТЬСЯ",
            variant = MangaButtonVariant.Ghost,
            onClick = { if (!busy) onJoin(code) },
            modifier = Modifier.fillMaxWidth(),
            minHeight = 52.dp,
        )

        if (state.phase == DuelPhase.Error && state.errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = state.errorMessage,
                color = QuizColors.modeShit,
                fontFamily = RussoOneFamily,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(24.dp))
        MangaButton(
            label = "ОТМЕНА",
            variant = MangaButtonVariant.Ghost,
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 48.dp,
        )
    }
}

@Composable
private fun CodeField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color.White)
                .border(QuizStrokes.panel, QuizColors.ink, shape)
                .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                onValueChange(raw.filter { it.isLetterOrDigit() }.uppercase().take(6))
            },
            singleLine = true,
            cursorBrush = SolidColor(QuizColors.ink),
            textStyle =
                TextStyle(
                    color = QuizColors.ink,
                    fontFamily = RussoOneFamily,
                    fontSize = 28.sp,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center,
                ),
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = "______",
                        fontFamily = RussoOneFamily,
                        fontSize = 28.sp,
                        letterSpacing = 8.sp,
                        color = QuizColors.ink.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                inner()
            },
        )
    }
}
