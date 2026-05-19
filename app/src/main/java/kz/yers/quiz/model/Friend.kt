package kz.yers.quiz.model

/** Public profile of a player, read from their `leaderboard/{uid}` doc. */
data class FriendProfile(
    val uid: String,
    val name: String,
    val bestScore: Int,
)

/** A friend row as shown on the Friends screen (local entry + best-effort live score). */
data class FriendCard(
    val uid: String,
    val name: String,
    /** Null when the live score couldn't be fetched (offline / never played a solo run). */
    val bestScore: Int?,
)

sealed interface AddFriendResult {
    data object Ok : AddFriendResult

    data object NotFound : AddFriendResult

    data object Self : AddFriendResult

    data object Offline : AddFriendResult
}
