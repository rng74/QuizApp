package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kz.yers.quiz.model.DailyState
import kz.yers.quiz.ui.composable.manga.MangaButton
import kz.yers.quiz.ui.composable.manga.MangaButtonVariant
import kz.yers.quiz.ui.composable.manga.MangaChip
import kz.yers.quiz.ui.composable.manga.MangaChipVariant
import kz.yers.quiz.ui.composable.manga.SpeechBubble
import kz.yers.quiz.ui.composable.manga.SpeedLines
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
fun DailyChallengeScreen(
    state: DailyState,
    onBack: () -> Unit,
    onPlay: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        ) {
            item {
                DailyAppBar(onBack = onBack)
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    DailyHero(state = state, onPlay = onPlay)
                    CountdownStrip()
                    StreakStrip(state.streakDays)
                    StatsRow(state = state)
                    state.previousTrackTitle?.let {
                        SpeechBubble(text = "Прошлый трек: «$it»")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyAppBar(onBack: () -> Unit) {
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
        IconButtonBack(onClick = onBack)
        Text(
            text = "ДНЕВНОЙ ВЫЗОВ",
            modifier = Modifier.weight(1f),
            color = QuizColors.ink,
            fontFamily = RussoOneFamily,
            fontSize = 18.sp,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun IconButtonBack(onClick: () -> Unit) {
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
        Text(
            text = "←",
            fontSize = 22.sp,
            color = QuizColors.ink,
        )
    }
}

@Composable
private fun DailyHero(
    state: DailyState,
    onPlay: () -> Unit,
) {
    val played = state.attempt != null
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(end = QuizShadows.large, bottom = QuizShadows.large)
                .drawBehind {
                    val o = QuizShadows.large.toPx()
                    val r = QuizRadii.button.toPx()
                    drawRoundRect(
                        color = QuizColors.ink,
                        topLeft = Offset(o, o),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(r, r),
                    )
                }
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD35B), QuizColors.tint),
                    ),
                )
                .border(QuizStrokes.panel, QuizColors.ink, shape),
    ) {
        SpeedLines(
            modifier = Modifier.fillMaxSize(),
            color = QuizColors.ink,
            opacity = 0.18f,
            angleDegrees = 85f,
            spacingPx = 28f,
            strokeWidthPx = 2f,
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = state.today.uppercase(),
                fontFamily = RussoOneFamily,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = QuizColors.ink.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "СЕГОДНЯ",
                fontFamily = BangersFamily,
                fontSize = 48.sp,
                lineHeight = 48.sp,
                letterSpacing = 1.sp,
                color = QuizColors.ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text =
                    if (played) {
                        "Вы уже сыграли сегодня. Возвращайтесь завтра!"
                    } else {
                        "Один трек на всех. Угадай быстрее всех — попадёшь в топ."
                    },
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = QuizColors.ink,
            )
            Spacer(Modifier.height(14.dp))
            if (played) {
                val a = state.attempt!!
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MangaChip(
                        label = if (a.correct) "Угадал" else "Мимо",
                        variant = if (a.correct) MangaChipVariant.Default else MangaChipVariant.Red,
                    )
                    MangaChip(label = "${a.score} очков", variant = MangaChipVariant.Ink)
                }
            } else {
                MangaButton(
                    label = "ИГРАТЬ",
                    variant = MangaButtonVariant.Ghost,
                    onClick = onPlay,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MangaChip(label = "⏱ ~30 сек")
                    MangaChip(label = "★ 2× очки", variant = MangaChipVariant.Red)
                }
            }
        }
    }
}

@Composable
private fun CountdownStrip() {
    var remaining by remember { mutableLongStateOf(secondsToNextUtcMidnight()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            remaining = secondsToNextUtcMidnight()
        }
    }
    val hours = (remaining / 3600).coerceAtLeast(0L)
    val minutes = ((remaining % 3600) / 60).coerceAtLeast(0L)
    val seconds = (remaining % 60).coerceAtLeast(0L)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "ДО СЛЕДУЮЩЕГО ВЫЗОВА",
            fontFamily = RussoOneFamily,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = QuizColors.ink.copy(alpha = 0.6f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CountdownCell(value = "%02d".format(hours), label = "час", modifier = Modifier.weight(1f))
            CountdownCell(value = "%02d".format(minutes), label = "мин", modifier = Modifier.weight(1f))
            CountdownCell(value = "%02d".format(seconds), label = "сек", modifier = Modifier.weight(1f))
            CountdownCell(value = "●", label = "live", modifier = Modifier.weight(1f), valueColor = QuizColors.streakFire)
        }
    }
}

@Composable
private fun CountdownCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = QuizColors.ink,
) {
    val shape = RoundedCornerShape(QuizRadii.card)
    Column(
        modifier =
            modifier
                .clip(shape)
                .background(Color.White)
                .border(QuizStrokes.regular, QuizColors.ink, shape)
                .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontFamily = RussoOneFamily,
            fontSize = 18.sp,
            color = valueColor,
        )
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = QuizColors.ink.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun StreakStrip(streakDays: Int) {
    val labels = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")
    val today = ((java.time.LocalDate.now().dayOfWeek.value + 6) % 7) // monday=0
    val doneCount = streakDays.coerceIn(0, 7)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "СЕРИЯ · $streakDays ДН.",
            fontFamily = RussoOneFamily,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = QuizColors.ink.copy(alpha = 0.6f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            labels.forEachIndexed { i, label ->
                val state =
                    when {
                        i == today -> kz.yers.quiz.ui.composable.manga.StreakDayState.Today
                        i < doneCount -> kz.yers.quiz.ui.composable.manga.StreakDayState.Done
                        else -> kz.yers.quiz.ui.composable.manga.StreakDayState.Empty
                    }
                kz.yers.quiz.ui.composable.manga.StreakDay(
                    label = label,
                    state = state,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (streakDays in 1..6) {
            val nextBonusInDays = (7 - streakDays).coerceAtLeast(1)
            Text(
                text = "Через $nextBonusInDays дн. — бонус +50 монет 🎁",
                fontSize = 12.sp,
                color = QuizColors.ink.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatsRow(state: DailyState) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatPanel(
            label = "место",
            value = state.attempt?.let { "—" } ?: "—",
            modifier = Modifier.weight(1f),
        )
        StatPanel(
            label = "играют",
            value = "—",
            modifier = Modifier.weight(1f),
        )
        StatPanel(
            label = "угадали",
            value = "—",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatPanel(
    label: String,
    value: String,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(QuizRadii.card)
    Box(
        modifier =
            modifier
                .padding(end = QuizShadows.small, bottom = QuizShadows.small)
                .drawBehind {
                    val o = QuizShadows.small.toPx()
                    val r = QuizRadii.card.toPx()
                    drawRoundRect(
                        color = QuizColors.ink,
                        topLeft = Offset(o, o),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(r, r),
                    )
                }
                .clip(shape)
                .background(Color.White)
                .border(QuizStrokes.panel, QuizColors.ink, shape)
                .padding(10.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value,
                fontFamily = RussoOneFamily,
                fontSize = 22.sp,
                color = QuizColors.ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = QuizColors.ink.copy(alpha = 0.6f),
            )
        }
    }
}

private fun secondsToNextUtcMidnight(): Long {
    val now = LocalDateTime.now(ZoneId.of("UTC"))
    val tomorrow = now.toLocalDate().plusDays(1).atStartOfDay()
    return tomorrow.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC)
}
