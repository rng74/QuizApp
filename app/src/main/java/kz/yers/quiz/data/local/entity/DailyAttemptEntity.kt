package kz.yers.quiz.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_attempt")
data class DailyAttemptEntity(
    @PrimaryKey
    val epochDay: Long,
    val score: Int,
    val durationMs: Long,
    // "Solved" for the shared daily stats — see QuizAppViewModel.DAILY_SOLVED_MIN.
    val correct: Boolean,
    // Titles of the day's tracks, joined with " · " (a single title before DB v5).
    val trackTitle: String,
    val createdAt: Long = System.currentTimeMillis(),
    // Added in DB v5 (multi-track daily). Pre-v5 rows were single-track.
    @ColumnInfo(defaultValue = "0")
    val correctCount: Int = 0,
    @ColumnInfo(defaultValue = "1")
    val totalTracks: Int = 1,
)
