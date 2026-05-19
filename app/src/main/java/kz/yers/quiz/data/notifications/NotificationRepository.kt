package kz.yers.quiz.data.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kz.yers.quiz.MainActivity
import kz.yers.quiz.R
import kz.yers.quiz.data.local.dao.NotificationDao
import kz.yers.quiz.data.local.entity.NotificationEntity

/**
 * Single entry point for notifications: writes the in-app inbox row (Room) and,
 * when allowed, mirrors it to the system tray. The unread badge and the inbox
 * screen both observe the DAO flows here.
 */
class NotificationRepository(
    private val context: Context,
    private val dao: NotificationDao,
) {
    fun observe(): Flow<List<NotificationEntity>> = dao.observeAll()

    fun unreadCount(): Flow<Int> = dao.observeUnreadCount()

    suspend fun markAllRead() = dao.markAllRead()

    /**
     * Record an event in the inbox and (best-effort) raise a system notification.
     * Safe to call from any dispatcher; no-op for the tray if the runtime
     * permission is missing.
     */
    suspend fun notify(
        type: NotificationType,
        title: String,
        body: String,
        channel: String = NotificationChannels.EVENTS,
        systemTray: Boolean = true,
    ) {
        dao.insert(NotificationEntity(type = type.name, title = title, body = body))
        dao.prune()
        if (systemTray) postToTray(type, title, body, channel)
    }

    private fun postToTray(
        type: NotificationType,
        title: String,
        body: String,
        channel: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationChannels.ensure(context)
        val destination =
            when (type) {
                NotificationType.DAILY_REMINDER,
                NotificationType.STREAK_AT_RISK,
                NotificationType.DAILY_DONE,
                -> MainActivity.DEST_DAILY

                else -> MainActivity.DEST_NOTIFICATIONS
            }
        val tapIntent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_DESTINATION, destination)
            }
        val pending =
            PendingIntent.getActivity(
                context,
                type.ordinal,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.launch)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(type.ordinal, notification)
        }
    }
}
