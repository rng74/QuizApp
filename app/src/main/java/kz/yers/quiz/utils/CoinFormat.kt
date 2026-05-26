package kz.yers.quiz.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Russian-locale digit grouping for coin balances ("12 500" instead of "12500").
 * Below 1000 we render bare digits — three-or-fewer-digit groupings look weird
 * when separator-padded. Cached formatter; safe to call on the composition thread.
 */
private val RU = Locale.forLanguageTag("ru")
private val FORMATTER: NumberFormat = NumberFormat.getInstance(RU)

fun formatCoins(coins: Int): String =
    if (coins < 1000) coins.toString() else FORMATTER.format(coins)
