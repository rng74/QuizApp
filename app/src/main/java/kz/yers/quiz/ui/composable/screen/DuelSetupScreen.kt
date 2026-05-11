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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onStart: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    var nameOne by remember { mutableStateOf("") }
    var nameTwo by remember { mutableStateOf("") }

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
        ImpactText(
            text = "ДУЭЛЬ",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Два игрока · одно устройство · одни вопросы",
            color = QuizColors.ink,
            fontFamily = RussoOneFamily,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(24.dp))

        MangaPanel(
            modifier = Modifier.fillMaxWidth(),
            background = QuizColors.paper2,
            shadowOffset = QuizShadows.medium,
            contentPadding = 18.dp,
        ) {
            Column {
                PlayerNameField(
                    label = "ИГРОК 1",
                    value = nameOne,
                    placeholder = "Игрок 1",
                    onValueChange = { nameOne = it },
                )
                Spacer(Modifier.height(14.dp))
                PlayerNameField(
                    label = "ИГРОК 2",
                    value = nameTwo,
                    placeholder = "Игрок 2",
                    onValueChange = { nameTwo = it },
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MangaChip(label = "10 вопросов")
            MangaChip(label = "Норм >8")
            MangaChip(label = "Сразу выбывание")
        }

        Spacer(Modifier.height(28.dp))

        MangaButton(
            label = "НАЧАТЬ ДУЭЛЬ",
            variant = MangaButtonVariant.Tint,
            onClick = { onStart(nameOne, nameTwo) },
            modifier = Modifier.fillMaxWidth(),
            minHeight = 56.dp,
        )
        Spacer(Modifier.height(12.dp))
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
private fun PlayerNameField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        Text(
            text = label,
            fontFamily = RussoOneFamily,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = QuizColors.ink.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(6.dp))
        val shape = RoundedCornerShape(QuizRadii.button)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(androidx.compose.ui.graphics.Color.White)
                    .border(QuizStrokes.panel, QuizColors.ink, shape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { onValueChange(it.take(20)) },
                singleLine = true,
                cursorBrush = SolidColor(QuizColors.ink),
                textStyle =
                    TextStyle(
                        color = QuizColors.ink,
                        fontFamily = RussoOneFamily,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp,
                    ),
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = RussoOneFamily,
                            fontSize = 16.sp,
                            color = QuizColors.ink.copy(alpha = 0.35f),
                        )
                    }
                    inner()
                },
            )
        }
    }
}
