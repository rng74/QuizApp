package kz.yers.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_attempt")
data class DailyAttemptEntity(
    @PrimaryKey
    val epochDay: Long,
    val score: Int,
    val durationMs: Long,
    val correct: Boolean,
    val trackTitle: String,
    val createdAt: Long = System.currentTimeMillis(),
)
