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
}
