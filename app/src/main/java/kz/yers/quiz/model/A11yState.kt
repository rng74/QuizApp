package kz.yers.quiz.model

import androidx.compose.runtime.staticCompositionLocalOf

data class A11yState(
    val reduceMotion: Boolean = false,
    val colorBlindSafe: Boolean = false,
    val largerText: Boolean = false,
    val dyslexiaFont: Boolean = false,
)

val LocalA11y = staticCompositionLocalOf { A11yState() }
