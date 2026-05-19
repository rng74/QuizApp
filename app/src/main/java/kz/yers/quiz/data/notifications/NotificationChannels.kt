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
            NotificationChannel(
                REMINDERS,
                "Напоминания",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Дневной вызов и серия" },
        )
    }
}
