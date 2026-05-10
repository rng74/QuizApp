package kz.yers.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kz.yers.quiz.data.local.entity.RunHistoryEntity

@Dao
interface RunHistoryDao {
    @Insert
    suspend fun insert(entity: RunHistoryEntity): Long

    @Query("SELECT * FROM run_history ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 10): Flow<List<RunHistoryEntity>>

    @Query("SELECT * FROM run_history ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 10): List<RunHistoryEntity>

    @Query("SELECT MAX(score) FROM run_history WHERE mode = :mode")
    suspend fun bestScoreForMode(mode: String): Int?

    @Query("SELECT MAX(score) FROM run_history")
    suspend fun overallBestScore(): Int?

    @Query("SELECT COUNT(*) FROM run_history")
    suspend fun totalRuns(): Int

    @Query("SELECT SUM(score) FROM run_history")
    suspend fun totalScore(): Int?

    @Query("SELECT COUNT(*) FROM run_history WHERE mode = :mode")
    suspend fun runsForMode(mode: String): Int
}
