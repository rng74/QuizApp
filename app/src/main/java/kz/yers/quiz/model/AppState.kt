package kz.yers.quiz.model

sealed class AppState(val name: String) {

    data object Menu : AppState("menu")

    data class Quiz(
        val currentQuestion: QuizQuestion,
        val currentQuestionIndex: Int
    ) : AppState("quiz") {

        companion object {
            const val name = "quiz"
        }
    }

    data object Loading : AppState("loading")

    data object Result : AppState("result")
}