package kz.yers.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kz.yers.quiz.data.local.entity.DailyAttemptEntity

@Dao
interface DailyAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyAttemptEntity)

    @Query("SELECT * FROM daily_attempt WHERE epochDay = :epochDay LIMIT 1")
    suspend fun forDay(epochDay: Long): DailyAttemptEntity?

    @Query("SELECT * FROM daily_attempt WHERE epochDay = :epochDay LIMIT 1")
    suspend fun yesterday(epochDay: Long): DailyAttemptEntity?
}
