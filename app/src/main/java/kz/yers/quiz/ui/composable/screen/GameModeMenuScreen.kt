package kz.yers.quiz.ui.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kz.yers.quiz.R
import kz.yers.quiz.model.DailyLiveStats
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.ui.composable.manga.MangaIcons
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun GameModeMenuScreen(
    highScore: Int,
    streakDays: Int,
    bestScoreByMode: Map<GameMode, Int>,
    recordModeJustSet: GameMode?,
    dailyStats: DailyLiveStats? = null,
    onGameModeSelected: (GameMode) -> Unit,
    onOpenDaily: () -> Unit = {},
    onOpenDuel: () -> Unit = {},
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
        DailyHero(stats = dailyStats, onClick = onOpenDaily)
        StreakRow(streakDays = streakDays, highScore = highScore)
        SectionHeading()
        ModeGrid(
            bestScoreByMode = bestScoreByMode,
            recordModeJustSet = recordModeJustSet,
            onGameModeSelected = onGameModeSelected,
        )
        DuelCta(onClick = onOpenDuel)
        Spacer(Modifier.height(4.dp))
    }
}

/** Bordered surface with an offset rectangle ink shadow (shared manga chrome). */
@Composable
private fun OffsetCard(
    modifier: Modifier = Modifier,
    background: Brush,
    shadowOffset: Dp,
    radius: Dp,
    border: Dp = QuizStrokes.panel,
    onClick: (() -> Unit)? = null,
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
                .border(border, QuizColors.ink, shape)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        content()
    }
}

private fun solid(color: Color) = Brush.linearGradient(listOf(color, color))

@Composable
private fun DailyHero(
    stats: DailyLiveStats?,
    onClick: () -> Unit,
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }
    val zone = ZoneId.systemDefault()
    val nextMidnight =
        LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val remaining = ((nextMidnight - nowMs) / 1000L).coerceAtLeast(0L)
    val hh = remaining / 3600L
    val mm = (remaining % 3600L) / 60L
    val ss = remaining % 60L

    OffsetCard(
        modifier = Modifier.fillMaxWidth(),
        background =
            Brush.linearGradient(
                listOf(QuizColors.tintGlow, QuizColors.tint, QuizColors.tintDeep),
            ),
        shadowOffset = QuizShadows.medium,
        radius = 14.dp,
        onClick = onClick,
    ) {
        Box {
            Text(
                text = "ドン!!",
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 14.dp)
                        .rotate(8f),
                fontFamily = BangersFamily,
                fontSize = 22.sp,
                color = QuizColors.ink,
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier =
                            Modifier
                                .rotate(-4f)
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(QuizStrokes.panel, QuizColors.ink, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MangaIcons.Daily,
                            contentDescription = null,
                            tint = QuizColors.ink,
                            modifier = Modifier.size(42.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(QuizColors.ink)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "2× ОЧКИ СЕГОДНЯ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp,
                                color = Color.White,
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "ДНЕВНОЙ ВЫЗОВ",
                            fontFamily = BangersFamily,
                            fontSize = 24.sp,
                            letterSpacing = 1.5.sp,
                            color = QuizColors.ink,
                        )
                        Text(
                            text = "Один трек на всех · топ-100 получают монеты",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = QuizColors.ink.copy(alpha = 0.75f),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    CountdownCell(hh.toString().padStart(2, '0'), "час", Modifier.weight(1f))
                    CountdownCell(mm.toString().padStart(2, '0'), "мин", Modifier.weight(1f))
                    CountdownCell(ss.toString().padStart(2, '0'), "сек", Modifier.weight(1f))
                    CountdownCell("▶", "играть", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier =
                            Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(QuizColors.streakFire),
                    )
                    Text(
                        text =
                            when {
                                stats == null || stats.players == 0 -> "Сыграй первым сегодня"
                                stats.myRank != null ->
                                    "${stats.players} играют сегодня · ваше место #${stats.myRank}"
                                else -> "${stats.players} играют сегодня"
                            },
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = QuizColors.ink.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownCell(
    value: String,
    label: String,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(QuizStrokes.regular, QuizColors.ink, RoundedCornerShape(6.dp))
                .padding(vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontFamily = RussoOneFamily,
                fontSize = 16.sp,
                color = QuizColors.ink,
            )
            Text(
                text = label.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                letterSpacing = 1.sp,
                color = QuizColors.ink.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun StreakRow(
    streakDays: Int,
    highScore: Int,
) {
    OffsetCard(
        modifier = Modifier.fillMaxWidth(),
        background = solid(Color.White),
        shadowOffset = QuizShadows.small + QuizStrokes.thin,
        radius = 12.dp,
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
                        .background(
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        QuizColors.flameHot,
                                        QuizColors.flameMid,
                                        QuizColors.streakFire,
                                    ),
                            ),
                        )
                        .border(QuizStrokes.panel, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MangaIcons.Flame,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "СЕРИЯ · $streakDays ${dayWord(streakDays)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    val onCount = (streakDays - 1).coerceIn(0, 7)
                    val todayIndex = if (streakDays in 1..7) streakDays - 1 else -1
                    repeat(7) { i ->
                        val color =
                            when {
                                i == todayIndex -> QuizColors.streakFire
                                i < onCount -> QuizColors.tint
                                else -> Color.White
                            }
                        Box(
                            modifier =
                                Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                                    .border(QuizStrokes.thin, QuizColors.ink, RoundedCornerShape(3.dp)),
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "РЕКОРД",
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.ink.copy(alpha = 0.6f),
                )
                Text(
                    text = highScore.toString(),
                    fontFamily = RussoOneFamily,
                    fontSize = 26.sp,
                    color = QuizColors.accentBlue,
                )
            }
        }
    }
}

private fun dayWord(n: Int): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> "дней"
        mod10 == 1 -> "день"
        mod10 in 2..4 -> "дня"
        else -> "дней"
    }
}

@Composable
private fun SectionHeading() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier =
                Modifier
                    .rotate(-1f)
                    .padding(end = QuizStrokes.regular, bottom = QuizStrokes.regular)
                    .drawBehind {
                        val o = QuizStrokes.regular.toPx()
                        val r = 6.dp.toPx()
                        drawRoundRect(
                            color = QuizColors.ink,
                            topLeft = Offset(o, o),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(r, r),
                        )
                    }
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .border(QuizStrokes.regular, QuizColors.ink, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
            Text(
                text = "РЕЖИМ ИГРЫ",
                fontFamily = BangersFamily,
                fontSize = 18.sp,
                letterSpacing = 1.5.sp,
                color = QuizColors.ink,
            )
        }
        Text(
            text = "тапни чтобы начать →",
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = QuizColors.ink.copy(alpha = 0.55f),
        )
    }
}

private data class ModeMeta(
    val desc: String,
    val valueColor: Color,
)

private fun modeMeta(mode: GameMode): ModeMeta =
    when (mode) {
        GameMode.EASY -> ModeMeta("Топовые ОПы", QuizColors.modeEasy)
        GameMode.NORMAL -> ModeMeta("Известные ОПы", QuizColors.tintDeep)
        GameMode.RANDOM -> ModeMeta("Любая песня", QuizColors.modeRandom)
        GameMode.SHIT -> ModeMeta("Редкие · хардкор", QuizColors.modeShit)
    }

@Composable
private fun ModeGrid(
    bestScoreByMode: Map<GameMode, Int>,
    recordModeJustSet: GameMode?,
    onGameModeSelected: (GameMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GameMode.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { mode ->
                    ModeCard(
                        mode = mode,
                        best = bestScoreByMode[mode] ?: 0,
                        isNew = recordModeJustSet == mode,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onGameModeSelected(mode) },
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ModeCard(
    mode: GameMode,
    best: Int,
    isNew: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val meta = modeMeta(mode)
    OffsetCard(
        modifier = modifier,
        background = solid(Color.White),
        shadowOffset = QuizShadows.small + QuizStrokes.thin,
        radius = 12.dp,
        onClick = onClick,
    ) {
        Box {
            Column(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = modeIconRes(mode)),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = mode.shortLabel.uppercase(),
                    fontFamily = RussoOneFamily,
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp,
                    color = QuizColors.ink,
                )
                Text(
                    text = meta.desc,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    lineHeight = 13.sp,
                    color = QuizColors.ink.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = QuizColors.ink.copy(alpha = 0.18f),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = QuizStrokes.thin.toPx(),
                                    pathEffect =
                                        PathEffect.dashPathEffect(
                                            floatArrayOf(6f, 6f),
                                        ),
                                )
                            }
                            .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "РЕКОРД",
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                        color = QuizColors.ink.copy(alpha = 0.5f),
                    )
                    Text(
                        text = best.toString(),
                        fontFamily = RussoOneFamily,
                        fontSize = 16.sp,
                        color = meta.valueColor,
                    )
                }
            }
            if (isNew) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp)
                            .rotate(4f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(QuizColors.streakFire)
                            .border(QuizStrokes.thin, QuizColors.ink, RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = "НОВЫЙ!",
                        fontFamily = BangersFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun DuelCta(onClick: () -> Unit) {
    OffsetCard(
        modifier = Modifier.fillMaxWidth(),
        background =
            Brush.linearGradient(
                listOf(QuizColors.duelStart, QuizColors.hintPurple, QuizColors.duelEnd),
            ),
        shadowOffset = QuizShadows.small + QuizStrokes.thin,
        radius = 12.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .rotate(-4f)
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(QuizStrokes.panel, QuizColors.ink, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MangaIcons.Versus,
                    contentDescription = null,
                    tint = QuizColors.ink,
                    modifier = Modifier.size(34.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ДУЭЛЬ",
                    fontFamily = BangersFamily,
                    fontSize = 20.sp,
                    letterSpacing = 1.5.sp,
                    color = Color.White,
                )
                Text(
                    text = "Два игрока · одно устройство",
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
            Box(
                modifier =
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(QuizStrokes.regular, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MangaIcons.ChevronRight,
                    contentDescription = null,
                    tint = QuizColors.ink,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun modeIconRes(mode: GameMode): Int =
    when (mode) {
        GameMode.EASY -> R.drawable.easy_icon
        GameMode.NORMAL -> R.drawable.normal_icon
        GameMode.RANDOM -> R.drawable.random_icon
        GameMode.SHIT -> R.drawable.hardcore_icon
    }
