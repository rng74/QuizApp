package kz.yers.quiz.model

/** Which side of an async duel this device is. The host creates the code; the guest joins it. */
enum class DuelRole { HOST, GUEST }

enum class DuelPhase {
    /** No duel in progress (setup screen). */
    Idle,

    /** A network op (create/join) is running. */
    Connecting,

    /** Code created/joined; lobby — may share code, wait, and start the round. */
    Lobby,

    /** This device finished its round; waiting on / showing the opponent's result. */
    Finished,

    /** Last create/join failed (offline, bad code, room full). */
    Error,
}

/**
 * Async, code-based duel. Both players answer the same seed-deterministic track solo on their
 * own device; scores sync through a Firestore snapshot listener (no server / FCM on Spark).
 */
data class DuelState(
    val phase: DuelPhase = DuelPhase.Idle,
    val code: String = "",
    val role: DuelRole = DuelRole.HOST,
    val seed: Long = 0L,
    val myName: String = "Игрок",
    val opponentName: String? = null,
    val myScore: Int? = null,
    val opponentScore: Int? = null,
    val errorMessage: String? = null,
    val totalQuestions: Int = DUEL_QUESTIONS,
) {
    val bothFinished: Boolean get() = myScore != null && opponentScore != null

    /** 0 = you win, 1 = opponent wins, null = draw. Only meaningful once [bothFinished]. */
    val winnerIndex: Int?
        get() {
            val me = myScore ?: return null
            val op = opponentScore ?: return null
            return when {
                me > op -> 0
                op > me -> 1
                else -> null
            }
        }

    companion object {
        const val DUEL_QUESTIONS = 10
    }
}
