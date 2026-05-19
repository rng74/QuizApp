package kz.yers.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kz.yers.quiz.data.local.entity.NotificationEntity

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(entity: NotificationEntity): Long

    @Query("SELECT * FROM notification ORDER BY createdAt DESC LIMIT 100")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notification WHERE read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE notification SET read = 1 WHERE read = 0")
    suspend fun markAllRead()

    /** Keep the table bounded: drop everything older than the newest [keep] rows. */
    @Query(
        "DELETE FROM notification WHERE id NOT IN " +
            "(SELECT id FROM notification ORDER BY createdAt DESC LIMIT :keep)",
    )
    suspend fun prune(keep: Int = 100)
}
