package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.BuildConfig
import kz.yers.quiz.model.A11yState
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.composable.manga.MangaSwitchRow
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily
import kz.yers.quiz.ui.theme.bodyFontFamily
import kz.yers.quiz.ui.theme.bodyScale
import kz.yers.quiz.ui.theme.errorColor
import kz.yers.quiz.ui.theme.successColor

data class SettingsActions(
    val onBack: () -> Unit,
    val onPosterToggle: (Boolean) -> Unit,
    val onSoundToggle: (Boolean) -> Unit,
    val onReduceMotionToggle: (Boolean) -> Unit,
    val onColorBlindToggle: (Boolean) -> Unit,
    val onLargerTextToggle: (Boolean) -> Unit,
    val onDyslexiaToggle: (Boolean) -> Unit,
    val onReplayTutorial: () -> Unit,
    val onResetHighScore: () -> Unit,
)

@Composable
fun SettingsScreen(
    isPosterEnabled: Boolean,
    soundEnabled: Boolean,
    a11y: A11yState,
    actions: SettingsActions,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper),
        contentPadding = PaddingValues(0.dp),
    ) {
        item { SettingsAppBar(onBack = actions.onBack) }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SettingsSection(title = "ИГРА") {
                    MangaSwitchRow(
                        label = "Постеры аниме",
                        description = "Показывать размытый постер во время трека",
                        checked = isPosterEnabled,
                        onCheckedChange = actions.onPosterToggle,
                    )
                    Divider()
                    MangaSwitchRow(
                        label = "Звуки",
                        description = "Эффекты при правильных ответах",
                        checked = soundEnabled,
                        onCheckedChange = actions.onSoundToggle,
                    )
                }

                SettingsSection(title = "ДОСТУПНОСТЬ") {
                    MangaSwitchRow(
                        label = "Уменьшить движение",
                        description = "Отключает конфетти, тряску и эффекты нажатий",
                        checked = a11y.reduceMotion,
                        onCheckedChange = actions.onReduceMotionToggle,
                    )
                    Divider()
                    MangaSwitchRow(
                        label = "Дальтоник-режим",
                        description = "Меняет правильный/неправильный на синий/оранжевый. Иконки видны всегда",
                        checked = a11y.colorBlindSafe,
                        onCheckedChange = actions.onColorBlindToggle,
                    )
                    Divider()
                    MangaSwitchRow(
                        label = "Крупный шрифт",
                        description = "Текст основного контента увеличивается на 20 %",
                        checked = a11y.largerText,
                        onCheckedChange = actions.onLargerTextToggle,
                    )
                    Divider()
                    MangaSwitchRow(
                        label = "Шрифт для дислексии",
                        description = "Lexend для основного текста (вместо системного)",
                        checked = a11y.dyslexiaFont,
                        onCheckedChange = actions.onDyslexiaToggle,
                    )
                    Spacer(Modifier.height(8.dp))
                    PreviewRow()
                }

                SettingsSection(title = "ПРОЧЕЕ") {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(onClick = actions.onReplayTutorial)
                                .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val scale = bodyScale()
                        val family = bodyFontFamily()
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Показать обучение снова",
                                color = QuizColors.ink,
                                fontWeight = FontWeight.Medium,
                                fontFamily = family,
                                fontSize = (15f * scale).sp,
                            )
                            Text(
                                text = "Покажет 3 экрана с правилами при следующем запуске",
                                color = QuizColors.ink.copy(alpha = 0.6f),
                                fontFamily = family,
                                fontSize = (12f * scale).sp,
                                lineHeight = (16f * scale).sp,
                            )
                        }
                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            color = QuizColors.ink,
                        )
                    }
                    Divider()
                    Spacer(Modifier.height(8.dp))
                    MangaButton(
                        label = "СБРОСИТЬ РЕКОРД",
                        variant = MangaButtonVariant.Ghost,
                        onClick = actions.onResetHighScore,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Text(
                    text = "Версия ${BuildConfig.VERSION_NAME} · сборка ${BuildConfig.VERSION_CODE}",
                    color = QuizColors.ink.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SettingsAppBar(onBack: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(QuizColors.paper)
                .drawBehind {
                    val s = QuizStrokes.panel.toPx()
                    drawLine(
                        color = QuizColors.ink,
                        start = Offset(0f, size.height - s / 2f),
                        end = Offset(size.width, size.height - s / 2f),
                        strokeWidth = s,
                    )
                }
                .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onBack)
        Text(
            text = "НАСТРОЙКИ",
            modifier = Modifier.weight(1f),
            color = QuizColors.ink,
            fontFamily = RussoOneFamily,
            fontSize = 18.sp,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun IconButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(shape)
                .background(Color.White)
                .border(QuizStrokes.regular, QuizColors.ink, shape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Назад",
            tint = QuizColors.ink,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            fontFamily = RussoOneFamily,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            color = QuizColors.ink.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(8.dp))
        MangaPanel(
            modifier = Modifier.fillMaxWidth(),
            shadowOffset = QuizShadows.medium,
            contentPadding = 12.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(QuizColors.ink.copy(alpha = 0.12f)),
    )
}

@Composable
private fun PreviewRow() {
    val correct = successColor()
    val wrong = errorColor()
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        FeedbackChip(label = "Верно!", icon = "✓", color = correct, modifier = Modifier.weight(1f))
        FeedbackChip(label = "Мимо…", icon = "✕", color = wrong, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FeedbackChip(
    label: String,
    icon: String,
    color: Color,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(QuizRadii.button)
    val scale = bodyScale()
    val family = bodyFontFamily()
    Row(
        modifier =
            modifier
                .clip(shape)
                .background(color.copy(alpha = 0.15f))
                .border(QuizStrokes.panel, color, shape)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, color = color, fontWeight = FontWeight.Bold, fontSize = (14f * scale).sp)
        Spacer(Modifier.size(6.dp))
        Text(
            text = label,
            color = QuizColors.ink,
            fontWeight = FontWeight.Medium,
            fontFamily = family,
            fontSize = (14f * scale).sp,
        )
    }
}
