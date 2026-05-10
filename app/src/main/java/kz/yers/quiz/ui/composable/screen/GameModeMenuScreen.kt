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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.R
import kz.yers.quiz.model.GameMode
import kz.yers.quiz.ui.composable.manga.ImpactText
import kz.yers.quiz.ui.composable.manga.MangaPanel
import kz.yers.quiz.ui.theme.BangersFamily
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizRadii
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

/**
 * Single-character badge / glyph style that disables the platform's extra font padding so the
 * glyph sits visually centered inside circular badges. Apply to Bangers/Russo One characters
 * sitting alone in a 40–56dp disc.
 */
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
fun GameModeMenuScreen(
    highScore: Int,
    onGameModeSelected: (GameMode) -> Unit,
    onOpenDaily: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val highScoreStr = stringResource(R.string.your_high_score_is, highScore)
    val selectGameModeStr = stringResource(R.string.select_game_mode)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.weight(1f))
            MenuIconButton(label = "ПРО", onClick = onOpenProfile)
            MenuIconButton(label = "⚙", onClick = onOpenSettings, fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        ImpactText(
            text = "АНИМЕ КВИЗ!",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Угадай аниме по саундтреку",
            color = QuizColors.ink,
            fontFamily = RussoOneFamily,
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))

        DailyEntryCard(onClick = onOpenDaily)
        Spacer(Modifier.height(20.dp))

        MangaPanel(
            modifier = Modifier.fillMaxWidth(),
            background = QuizColors.paper2,
            shadowOffset = QuizShadows.medium,
            contentPadding = 16.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "ЛУЧШИЙ СЧЁТ",
                        fontFamily = RussoOneFamily,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        color = QuizColors.ink.copy(alpha = 0.6f),
                    )
                    Text(
                        text = highScoreStr.substringAfter(": ", highScore.toString()),
                        fontFamily = BangersFamily,
                        fontSize = 36.sp,
                        color = QuizColors.accentBlue,
                        style = BadgeGlyphStyle,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(QuizColors.streakFire)
                            .border(QuizStrokes.panel, QuizColors.ink, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "★",
                        fontSize = 26.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = BadgeGlyphStyle,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = selectGameModeStr,
            fontFamily = RussoOneFamily,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
            color = QuizColors.ink,
        )
        Spacer(Modifier.height(12.dp))

        // 2×2 mode card grid.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val modes = GameMode.entries
            modes.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { mode ->
                        ModeCard(
                            mode = mode,
                            modifier = Modifier.weight(1f),
                            onClick = { onGameModeSelected(mode) },
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ModeCard(
    mode: GameMode,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(QuizRadii.button)
    val shadowOffset = QuizShadows.medium
    Box(
        modifier =
            modifier
                .padding(end = shadowOffset, bottom = shadowOffset)
                .drawBehind {
                    val o = shadowOffset.toPx()
                    val r = QuizRadii.button.toPx()
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
                .clickable(onClick = onClick)
                .padding(14.dp),
    ) {
        Column {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(mode.tint)
                        .border(QuizStrokes.panel, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.shortLabel.first().uppercase(),
                    fontFamily = BangersFamily,
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = BadgeGlyphStyle,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = mode.shortLabel.uppercase(),
                fontFamily = RussoOneFamily,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp,
                color = QuizColors.ink,
            )
            Text(
                text = mode.rule,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
                color = QuizColors.ink.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun MenuIconButton(
    label: String,
    onClick: () -> Unit,
    fontSize: androidx.compose.ui.unit.TextUnit = 11.sp,
) {
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
            text = label,
            fontFamily = RussoOneFamily,
            fontSize = fontSize,
            letterSpacing = 1.sp,
            color = QuizColors.ink,
            textAlign = TextAlign.Center,
            style = BadgeGlyphStyle,
        )
    }
}

@Composable
private fun DailyEntryCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(QuizRadii.button)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(end = QuizShadows.medium, bottom = QuizShadows.medium)
                .drawBehind {
                    val o = QuizShadows.medium.toPx()
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
                .border(QuizStrokes.panel, QuizColors.ink, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ДНЕВНОЙ ВЫЗОВ",
                    fontFamily = BangersFamily,
                    fontSize = 22.sp,
                    color = QuizColors.ink,
                    letterSpacing = 1.sp,
                    style = BadgeGlyphStyle,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Один трек на всех · 2× очки",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = QuizColors.ink.copy(alpha = 0.75f),
                )
            }
            Text(
                text = "→",
                fontFamily = BangersFamily,
                fontSize = 28.sp,
                color = QuizColors.ink,
                style = BadgeGlyphStyle,
            )
        }
    }
}
