package kz.yers.quiz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_history")
data class RunHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String,
    val score: Int,
    val durationMs: Long,
    val dateEpochDay: Long,
    val createdAt: Long = System.currentTimeMillis(),
)
