package kz.yers.quiz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kz.yers.quiz.data.local.dao.DailyAttemptDao
import kz.yers.quiz.data.local.dao.RunHistoryDao
import kz.yers.quiz.data.local.entity.DailyAttemptEntity
import kz.yers.quiz.data.local.entity.RunHistoryEntity

@Database(
    entities = [RunHistoryEntity::class, DailyAttemptEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runHistoryDao(): RunHistoryDao

    abstract fun dailyAttemptDao(): DailyAttemptDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "quiz.db",
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
