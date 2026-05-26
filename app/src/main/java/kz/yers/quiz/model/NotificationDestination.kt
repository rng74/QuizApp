package kz.yers.quiz.model

/**
 * Allowlist of in-app destinations a system notification tap may route to.
 *
 * Notifications are produced by [kz.yers.quiz.data.notifications.NotificationRepository] and
 * consumed by [kz.yers.quiz.MainActivity.routeFromIntent]; using an enum (rather than free-form
 * strings) means a typo on the producer side surfaces at compile time and an intent crafted by
 * a third party can only resolve to one of these whitelisted entries.
 */
enum class NotificationDestination {
    /** Notification inbox screen (default for most event types). */
    INBOX,

    /** Today's daily challenge screen. */
    DAILY,
    ;

    /** Stable string for the Intent extra. Use [parse] to round-trip back. */
    fun asExtra(): String = name

    companion object {
        const val EXTRA_KEY = "destination"

        /** Returns null for unknown/missing values — callers MUST treat null as "no routing". */
        fun parse(raw: String?): NotificationDestination? =
            entries.firstOrNull { it.name == raw }
    }
}
