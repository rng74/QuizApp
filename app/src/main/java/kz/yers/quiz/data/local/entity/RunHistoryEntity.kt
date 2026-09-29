package kz.yers.quiz.data.local.entity

import androidx.room.ColumnInfo
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
    // Added in DB v5. Rows written before that have answered = 0 and are excluded from accuracy.
    @ColumnInfo(defaultValue = "0")
    val correct: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val answered: Int = 0,
)
