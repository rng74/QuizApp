package kz.yers.quiz.model

data class DailyState(
    val epochDay: Long = 0L,
    val today: String = "",
    val streakDays: Int = 0,
    val attempt: DailyAttemptSummary? = null,
    val previousTrackTitle: String? = null,
    val liveStats: DailyLiveStats? = null,
)

/** Shared, cross-player stats for today's daily — null while loading or offline. */
data class DailyLiveStats(
    val players: Int,
    val solvedPct: Int,
    val myRank: Int?,
)

data class DailyAttemptSummary(
    val score: Int,
    val correct: Boolean,
    val durationMs: Long,
)

data class ProfileState(
    val userName: String = "Игрок",
    val totalGames: Int = 0,
    val currentStreakDays: Int = 0,
    val accuracyPct: Int = 0,
    val highScore: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val xpForNextLevel: Int = 1000,
    val recentGames: List<RecentGame> = emptyList(),
    val unlockedBadges: Set<AchievementId> = emptySet(),
)

data class RecentGame(
    val mode: GameMode?,
    val score: Int,
    val createdAt: Long,
)
