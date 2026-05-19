package kz.yers.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A friend the player added by code. The friend list is local-only: enrichment
 * (current best score) is fetched on demand from the public `leaderboard/{uid}`
 * doc, so no cross-writes to other players' docs are needed (Spark-friendly).
 */
@Entity(tableName = "friend")
data class FriendEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val addedAt: Long = System.currentTimeMillis(),
)
