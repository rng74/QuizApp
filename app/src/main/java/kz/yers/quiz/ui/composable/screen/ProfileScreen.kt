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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.model.AchievementId
import kz.yers.quiz.model.AchievementVariant
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.model.ProfileState
import kz.yers.quiz.model.RecentGame
import kz.yers.quiz.ui.composable.manga.MangaIcons
import kz.yers.quiz.ui.composable.manga.SpeedLines
import kz.yers.quiz.ui.composable.manga.icon
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
fun ProfileScreen(
    state: ProfileState,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper),
    ) {
        item {
            ProfileHeader(state = state, onBack = onBack)
        }
        item {
            Spacer(Modifier.height(0.dp))
            ProfileStatsRow(state = state, modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-44).dp))
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionTitle("Достижения · ${state.unlockedBadges.size} / ${AchievementId.entries.size}")
                Spacer(Modifier.height(10.dp))
                BadgeGrid(unlocked = state.unlockedBadges)
                Spacer(Modifier.height(20.dp))
                SectionTitle("Недавние игры")
                Spacer(Modifier.height(8.dp))
                if (state.recentGames.isEmpty()) {
                    Text(
                        text = "Пока пусто. Сыграйте первую игру!",
                        fontSize = 13.sp,
                        color = QuizColors.ink.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                } else {
                    state.recentGames.forEach { row ->
                        RecentRow(row)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    state: ProfileState,
    onBack: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(QuizColors.ink, Color(0xFF3A2A4A)),
                    ),
                )
                .drawBehind {
                    val s = QuizStrokes.panel.toPx()
                    drawLine(
                        color = QuizColors.ink,
                        start = Offset(0f, size.height - s / 2f),
                        end = Offset(size.width, size.height - s / 2f),
                        strokeWidth = s,
                    )
                },
    ) {
        SpeedLines(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            opacity = 0.18f,
            angleDegrees = 85f,
            spacingPx = 26f,
            strokeWidthPx = 1.4f,
        )
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 60.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButtonHeader(onClick = onBack)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "ПРОФИЛЬ",
                    color = Color.White,
                    fontFamily = RussoOneFamily,
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp,
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Avatar(initial = state.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?")
                Spacer(Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f).padding(bottom = 4.dp)) {
                    Text(
                        text = state.userName,
                        color = Color.White,
                        fontFamily = RussoOneFamily,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = "Лвл ${state.level} · ${levelTitle(state.level)}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            XpBar(
                xp = state.xp,
                xpForNext = state.xpForNextLevel,
                level = state.level,
            )
        }
    }
}

@Composable
private fun XpBar(
    xp: Int,
    xpForNext: Int,
    level: Int,
) {
    val progress = if (xpForNext == 0) 0f else (xp.toFloat() / xpForNext.toFloat()).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(QuizRadii.pill)
    Column {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(shape)
                    .background(Color.White.copy(alpha = 0.18f)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fraction = progress)
                        .height(6.dp)
                        .clip(shape)
                        .background(QuizColors.tint),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "XP до Лвл ${level + 1}: $xp / $xpForNext",
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun Avatar(initial: String) {
    Box(
        modifier =
            Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(QuizColors.tint, QuizColors.streakFire),
                    ),
                )
                .border(3.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            fontFamily = RussoOneFamily,
            fontSize = 36.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = BadgeGlyphStyle,
        )
    }
}

@Composable
private fun ProfileStatsRow(
    state: ProfileState,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile("игр", state.totalGames.toString(), modifier = Modifier.weight(1f))
        StatTile(
            label = "серия",
            value = state.currentStreakDays.toString(),
            modifier = Modifier.weight(1f),
            trailingIcon = MangaIcons.Flame,
            trailingIconTint = QuizColors.streakFire,
        )
        StatTile("рекорд", state.highScore.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier,
    trailingIcon: ImageVector? = null,
    trailingIconTint: Color = QuizColors.ink,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    fontFamily = RussoOneFamily,
                    fontSize = 22.sp,
                    color = QuizColors.ink,
                )
                if (trailingIcon != null) {
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = trailingIconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = QuizColors.ink.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        fontFamily = RussoOneFamily,
        fontSize = 14.sp,
        letterSpacing = 1.5.sp,
        color = QuizColors.ink,
    )
}

@Composable
private fun BadgeGrid(unlocked: Set<AchievementId>) {
    val all = AchievementId.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        all.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { id ->
                    Badge(id = id, unlocked = id in unlocked, modifier = Modifier.weight(1f))
                }
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Badge(
    id: AchievementId,
    unlocked: Boolean,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(QuizRadii.button)
    val (bg, content) =
        if (!unlocked) {
            QuizColors.paper2 to QuizColors.ink.copy(alpha = 0.35f)
        } else {
            when (id.variant) {
                AchievementVariant.Gold -> Color(0xFFFFC933) to Color.White
                AchievementVariant.Silver -> Color(0xFFB0B0B0) to Color.White
                AchievementVariant.Fire -> QuizColors.streakFire to Color.White
                AchievementVariant.Purple -> QuizColors.hintPurple to Color.White
                AchievementVariant.Regular -> Color.White to QuizColors.ink
            }
        }
    Box(
        modifier =
            modifier
                .padding(end = QuizShadows.small, bottom = QuizShadows.small)
                .drawBehind {
                    val o = QuizShadows.small.toPx()
                    val r = QuizRadii.button.toPx()
                    drawRoundRect(
                        color = QuizColors.ink,
                        topLeft = Offset(o, o),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(r, r),
                    )
                }
                .clip(shape)
                .background(bg)
                .border(QuizStrokes.regular, QuizColors.ink, shape),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = id.icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = id.title,
                fontFamily = RussoOneFamily,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp,
                color = content,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
            )
        }
    }
}

@Composable
private fun RecentRow(row: RecentGame) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModeDot(row.mode)
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${row.mode?.shortLabel ?: "Дневной"} · 30 вопр.",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = QuizColors.ink,
            )
            Text(
                text = formatRelativeDate(row.createdAt),
                fontSize = 11.sp,
                color = QuizColors.ink.copy(alpha = 0.6f),
            )
        }
        Text(
            text = row.score.toString(),
            fontFamily = RussoOneFamily,
            fontSize = 18.sp,
            color = QuizColors.ink,
        )
    }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(QuizColors.ink.copy(alpha = 0.12f)),
    )
}

@Composable
private fun ModeDot(mode: GameMode?) {
    val shape = RoundedCornerShape(QuizRadii.card)
    val color = mode?.tint ?: QuizColors.tint
    val icon = mode?.icon ?: MangaIcons.ModeEasy
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(shape)
                .background(color)
                .border(QuizStrokes.regular, QuizColors.ink, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun IconButtonHeader(onClick: () -> Unit) {
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(QuizStrokes.regular, Color.White, shape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Назад",
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

private fun levelTitle(level: Int): String =
    when {
        level >= 20 -> "Мастер"
        level >= 10 -> "Знаток"
        level >= 5 -> "Любитель"
        else -> "Новичок"
    }

private fun formatRelativeDate(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - epochMs
    val day = 24L * 60L * 60L * 1000L
    return when {
        diffMs < day -> "Сегодня"
        diffMs < 2 * day -> "Вчера"
        else -> {
            val fmt = SimpleDateFormat("d MMM", Locale.forLanguageTag("ru"))
            fmt.format(Date(epochMs))
        }
    }
}
