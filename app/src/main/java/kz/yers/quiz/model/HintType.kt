package kz.yers.quiz.model

enum class HintType(
    val coinPrice: Int,
) {
    FIFTY_FIFTY(50),
    REVEAL_LETTER(40),
    SKIP(80),
}

data class HintInventory(
    val fiftyFifty: Int = 0,
    val revealLetter: Int = 0,
    val skip: Int = 0,
) {
    fun countFor(type: HintType): Int =
        when (type) {
            HintType.FIFTY_FIFTY -> fiftyFifty
            HintType.REVEAL_LETTER -> revealLetter
            HintType.SKIP -> skip
        }

    fun withDecrement(type: HintType): HintInventory =
        when (type) {
            HintType.FIFTY_FIFTY -> copy(fiftyFifty = (fiftyFifty - 1).coerceAtLeast(0))
            HintType.REVEAL_LETTER -> copy(revealLetter = (revealLetter - 1).coerceAtLeast(0))
            HintType.SKIP -> copy(skip = (skip - 1).coerceAtLeast(0))
        }

    fun withIncrement(type: HintType): HintInventory =
        when (type) {
            HintType.FIFTY_FIFTY -> copy(fiftyFifty = fiftyFifty + 1)
            HintType.REVEAL_LETTER -> copy(revealLetter = revealLetter + 1)
            HintType.SKIP -> copy(skip = skip + 1)
        }
}

data class QuestionHintState(
    val eliminatedOptions: Set<String> = emptySet(),
    val revealedFirstLetter: String? = null,
    val fiftyFiftyUsed: Boolean = false,
    val revealUsed: Boolean = false,
)
