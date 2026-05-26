package kz.yers.quiz.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

/** OS notification channels. Two: in-app events vs. scheduled reminders. */
object NotificationChannels {
    const val EVENTS = "events"
    const val REMINDERS = "reminders"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                EVENTS,
                "События игры",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Рекорды, серии, результаты дуэлей" },
        )
        manager.createNotificationChannel(
            // HIGH so the streak-at-risk reminder surfaces as a heads-up — the
            // whole point of the channel is "don't miss this". Note: Android
            // freezes a channel's importance at creation; existing v3 installs
            // keep DEFAULT. The user can always tighten in system settings.
            NotificationChannel(
                REMINDERS,
                "Напоминания",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Дневной вызов и серия" },
        )
    }
}
