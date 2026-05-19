package kz.yers.quiz.model

sealed class AppState(
    val name: String,
) {
    data object Menu : AppState("menu")

    data class Quiz(
        val currentQuestion: QuizQuestion,
        val currentQuestionIndex: Int,
    ) : AppState(NAME) {
        companion object {
            const val NAME = "quiz"
        }
    }

    data object Loading : AppState("loading")

    data object Result : AppState("result")

    data object Daily : AppState("daily")

    data object Profile : AppState("profile")

    data object Leaderboard : AppState("leaderboard")

    data object Settings : AppState("settings")

    data object Notifications : AppState("notifications")

    data object Shop : AppState("shop")

    data object DuelSetup : AppState("duel_setup")

    data object DuelHandoff : AppState("duel_handoff")

    data object DuelResult : AppState("duel_result")
}
