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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.yers.quiz.data.local.entity.NotificationEntity
import kz.yers.quiz.data.notifications.NotificationType
import kz.yers.quiz.ui.composable.manga.MangaIcons
import kz.yers.quiz.ui.composable.manga.SpeechBubble
import kz.yers.quiz.ui.theme.QuizColors
import kz.yers.quiz.ui.theme.QuizShadows
import kz.yers.quiz.ui.theme.QuizStrokes
import kz.yers.quiz.ui.theme.RussoOneFamily

@Composable
fun NotificationInboxScreen(notifications: List<NotificationEntity>) {
    if (notifications.isEmpty()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(QuizColors.paper)
                    .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            SpeechBubble(text = "Пока тут пусто.\nИграй — и появятся новости!")
        }
        return
    }
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(QuizColors.paper)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(notifications, key = { it.id }) { item ->
            NotificationRow(item)
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationEntity) {
    val type = runCatching { NotificationType.valueOf(item.type) }.getOrNull()
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(end = QuizShadows.small, bottom = QuizShadows.small)
                .clip(shape)
                .background(if (item.read) QuizColors.paper2 else Color.White)
                .border(QuizStrokes.panel, QuizColors.ink, shape),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(type.tint())
                        .border(QuizStrokes.regular, QuizColors.ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = type.icon(),
                    contentDescription = null,
                    tint = QuizColors.ink,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f),
                        fontFamily = RussoOneFamily,
                        fontSize = 14.sp,
                        color = QuizColors.ink,
                    )
                    if (!item.read) {
                        Box(
                            modifier =
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(QuizColors.streakFire),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.body,
                    fontSize = 13.sp,
                    color = QuizColors.ink.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = relativeTime(item.createdAt),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    color = QuizColors.ink.copy(alpha = 0.45f),
                )
            }
        }
    }
}

private fun NotificationType?.icon(): ImageVector =
    when (this) {
        NotificationType.NEW_RECORD -> MangaIcons.Trophy
        NotificationType.DUEL_RESULT -> MangaIcons.Versus
        NotificationType.DAILY_DONE,
        NotificationType.DAILY_REMINDER,
        -> MangaIcons.Daily
        else -> MangaIcons.Bell
    }

private fun NotificationType?.tint(): Color =
    when (this) {
        NotificationType.NEW_RECORD -> QuizColors.tintGlow
        NotificationType.STREAK_MILESTONE,
        NotificationType.STREAK_AT_RISK,
        -> QuizColors.streakFire
        NotificationType.DUEL_RESULT -> QuizColors.hintPurple
        else -> QuizColors.paper2
    }

private fun relativeTime(epochMs: Long): String {
    val diff = System.currentTimeMillis() - epochMs
    val minutes = diff / 60_000L
    return when {
        minutes < 1 -> "ТОЛЬКО ЧТО"
        minutes < 60 -> "$minutes МИН НАЗАД"
        minutes < 24 * 60 -> "${minutes / 60} Ч НАЗАД"
        else -> "${minutes / (24 * 60)} ДН НАЗАД"
    }
}
