package kz.yers.quiz.utils

import android.content.Context
import android.content.Intent

private const val GREEN = "🟩" // 🟩 correct
private const val RED = "🟥" // 🟥 the miss
private const val ROW = 5
private const val PLAY_URL = "https://play.google.com/store/apps/details?id=kz.yers.quiz"

/**
 * Spoiler-safe Wordle-style grid: one square per question — 🟩 for each correct answer, a single
 * 🟥 for the run-ending miss (omitted on a perfect run). Wrapped at [ROW] per line. Never leaks
 * track titles, so it's safe to paste anywhere.
 */
fun emojiGridFor(
    correct: Int,
    total: Int,
): String {
    val safeCorrect = correct.coerceIn(0, total)
    val squares =
        buildList {
            repeat(safeCorrect) { add(GREEN) }
            if (safeCorrect < total) add(RED)
        }
    if (squares.isEmpty()) return RED
    return squares
        .chunked(ROW)
        .joinToString("\n") { it.joinToString("") }
}

/** The full "побей мой результат" challenge message: header + grid + score + store link. */
fun buildScoreShareText(
    score: Int,
    correct: Int,
    total: Int,
    isDaily: Boolean,
    modeName: String?,
): String {
    val header =
        when {
            isDaily -> "АНИМЕ КВИЗ · Дневной вызов"
            modeName != null -> "АНИМЕ КВИЗ · ${modeName.uppercase()}"
            else -> "АНИМЕ КВИЗ"
        }
    return buildString {
        appendLine(header)
        appendLine(emojiGridFor(correct, total))
        appendLine("Счёт: $score")
        appendLine("Побей мой результат 👇")
        append(PLAY_URL)
    }
}

fun shareText(
    context: Context,
    text: String,
    chooserTitle: String,
) {
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    val chooser =
        Intent.createChooser(send, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(chooser)
}
