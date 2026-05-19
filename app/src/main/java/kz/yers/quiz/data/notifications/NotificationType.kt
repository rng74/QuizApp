package kz.yers.quiz.data.notifications

/**
 * Every notification the app can raise. All are client-generated — cross-device
 * push (opponent finished while the app is closed) needs a server and stays out
 * of scope on the Firebase Spark plan (see V2_REDESIGN_PLAN.md, Stub #9).
 */
enum class NotificationType {
    /** A new 7-day-multiple streak milestone was reached. */
    STREAK_MILESTONE,

    /** A run beat the all-time high score. */
    NEW_RECORD,

    /** Today's daily challenge was completed. */
    DAILY_DONE,

    /** A duel finished (both scores known). */
    DUEL_RESULT,

    /** Scheduled: today's daily challenge hasn't been played yet. */
    DAILY_REMINDER,

    /** Scheduled: the daily streak will break if today is missed. */
    STREAK_AT_RISK,
}
