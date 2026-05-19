package kz.yers.quiz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kz.yers.quiz.data.local.entity.FriendEntity

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FriendEntity)

    @Query("SELECT * FROM friend ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friend ORDER BY addedAt DESC")
    suspend fun all(): List<FriendEntity>

    @Query("DELETE FROM friend WHERE uid = :uid")
    suspend fun delete(uid: String)
}
