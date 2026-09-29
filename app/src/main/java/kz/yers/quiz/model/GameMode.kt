package kz.yers.quiz.model

import androidx.compose.ui.graphics.Color
import kz.yers.quiz.ui.theme.QuizColors

enum class GameMode(
    val displayName: String,
    val shortLabel: String,
    val rule: String,
    val tint: Color,
) {
    EASY("Изи 8.3+", "Изи", "Только топовые ОПы", QuizColors.modeEasy),
    NORMAL("Норм 7.5+", "Норм", "Хорошо известные ОПы", QuizColors.modeNormal),
    RANDOM("Рандом ~", "Рандом", "Любая песня · непредсказуемо", QuizColors.modeRandom),
    SHIT("Шарю ≤6", "Шарю", "Редкие · OP, ED и OST", QuizColors.modeShit),
}
