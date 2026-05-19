package kz.yers.quiz.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kz.yers.quiz.data.local.dao.DailyAttemptDao
import kz.yers.quiz.data.prefs.UserPrefs
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Once-a-day check (no server): if today's daily challenge is still unplayed,
 * remind the player — and warn louder when an active streak is about to break.
 */
class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {
    private val dailyAttemptDao: DailyAttemptDao by inject()
    private val userPrefs: UserPrefs by inject()
    private val notifications: NotificationRepository by inject()

    override suspend fun doWork(): Result {
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val playedToday = dailyAttemptDao.forDay(today) != null
        if (playedToday) return Result.success()

        val streak = userPrefs.currentStreakDays.first()
        val lastPlayed = userPrefs.lastPlayedEpochDay.first()
        val streakAtRisk = streak > 0 && lastPlayed == today - 1

        if (streakAtRisk) {
            notifications.notify(
                type = NotificationType.STREAK_AT_RISK,
                title = "Серия под угрозой! 🔥",
                body = "Сыграй дневной вызов сегодня, чтобы не потерять серию из $streak дней.",
                channel = NotificationChannels.REMINDERS,
            )
        } else {
            notifications.notify(
                type = NotificationType.DAILY_REMINDER,
                title = "Дневной вызов ждёт 🎵",
                body = "Угадай сегодняшний трек и продолжи серию.",
                channel = NotificationChannels.REMINDERS,
            )
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily_reminder"
        private const val REMINDER_HOUR = 19

        /** Idempotent: enqueues a ~daily check, first fire at the next 19:00 local. */
        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            var next = now.with(LocalTime.of(REMINDER_HOUR, 0))
            if (!next.isAfter(now)) next = next.plusDays(1)
            val initialDelayMinutes =
                java.time.Duration.between(now, next).toMinutes().coerceAtLeast(1)

            val request =
                PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
