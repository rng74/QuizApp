package kz.yers.quiz.model

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val mode: String,
    val score: Int,
    val isYou: Boolean,
)

sealed interface LeaderboardUiState {
    data object Loading : LeaderboardUiState

    data class Loaded(
        val rows: List<LeaderboardEntry>,
        val myRank: Int?,
        val myScore: Int,
        val totalPlayers: Int,
    ) : LeaderboardUiState

    /** Network/Firestore unreachable or backend not yet provisioned. */
    data object Offline : LeaderboardUiState
}
