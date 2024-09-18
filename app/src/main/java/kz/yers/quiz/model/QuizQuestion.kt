package kz.yers.quiz.model

data class QuizQuestion(
    val correctAnswer: AnimeInfo,
    val options: List<String>
)
