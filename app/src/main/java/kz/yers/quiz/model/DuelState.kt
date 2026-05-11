package kz.yers.quiz.model

data class DuelPlayer(
    val name: String,
    val score: Int = 0,
    val answeredQuestions: Int = 0,
)

data class DuelState(
    val players: List<DuelPlayer> = listOf(DuelPlayer("Игрок 1"), DuelPlayer("Игрок 2")),
    val currentPlayerIndex: Int = 0,
    val totalQuestions: Int = DUEL_QUESTIONS,
) {
    val currentPlayer: DuelPlayer get() = players[currentPlayerIndex]

    val winnerIndex: Int?
        get() {
            val (p1, p2) = players[0] to players[1]
            return when {
                p1.score > p2.score -> 0
                p2.score > p1.score -> 1
                else -> null
            }
        }

    companion object {
        const val DUEL_QUESTIONS = 10
    }
}
