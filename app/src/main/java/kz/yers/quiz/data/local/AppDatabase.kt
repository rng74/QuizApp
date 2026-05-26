package kz.yers.quiz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kz.yers.quiz.data.local.dao.DailyAttemptDao
import kz.yers.quiz.data.local.dao.FriendDao
import kz.yers.quiz.data.local.dao.NotificationDao
import kz.yers.quiz.data.local.dao.RunHistoryDao
import kz.yers.quiz.data.local.entity.DailyAttemptEntity
import kz.yers.quiz.data.local.entity.FriendEntity
import kz.yers.quiz.data.local.entity.NotificationEntity
import kz.yers.quiz.data.local.entity.RunHistoryEntity

@Database(
    entities = [
        RunHistoryEntity::class,
        DailyAttemptEntity::class,
        NotificationEntity::class,
        FriendEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runHistoryDao(): RunHistoryDao

    abstract fun dailyAttemptDao(): DailyAttemptDao

    abstract fun notificationDao(): NotificationDao

    abstract fun friendDao(): FriendDao

    companion object {
        // v4 is the baseline released schema. Any future schema bump MUST add a
        // Migration in Migrations.kt and pass it via .addMigrations(…). Removing
        // the destructive fallback means a missing migration will crash at
        // startup — that's intentional: it surfaces the regression at dev time
        // instead of silently wiping every player's runs/streaks/friends/coins.
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "quiz.db",
            )
                .addMigrations(*Migrations.ALL)
                .build()
    }
}
