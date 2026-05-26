package kz.yers.quiz.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Thin wrapper around [FirebaseAnalytics]. Provides typed event/param constants
 * and a single `log(name, params)` entry point. Owns the SDK singleton so callers
 * don't have to plumb a [Context] through every layer.
 *
 * Schema policy:
 *  - Event names: lower_snake_case, < 40 chars (Firebase limit).
 *  - Param names: lower_snake_case, < 40 chars; value types limited to
 *    String / Long / Double / Int / Bool (Bundle).
 *  - NO PII: never send player names, friend uids, anime titles, raw search text.
 *  - Counts and categorical buckets, not free text.
 *
 * Call sites should reference [Events] + [Params] constants — typo-proof and
 * one place to audit the analytics schema before submitting to Firebase console.
 */
class Analytics(context: Context) {
    private val fa: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    init {
        // App-level toggle. Per-user opt-out would go through a UserPrefs flag
        // wired to setAnalyticsCollectionEnabled(false); we don't expose one yet.
        fa.setAnalyticsCollectionEnabled(true)
    }

    /** Single entry point for arbitrary events. Drops nulls silently. */
    fun log(
        event: String,
        vararg params: Pair<String, Any?>,
    ) {
        val bundle =
            Bundle().apply {
                params.forEach { (k, v) ->
                    when (v) {
                        null -> Unit
                        is String -> putString(k, v.take(100))
                        is Int -> putLong(k, v.toLong())
                        is Long -> putLong(k, v)
                        is Float -> putDouble(k, v.toDouble())
                        is Double -> putDouble(k, v)
                        is Boolean -> putLong(k, if (v) 1L else 0L)
                        else -> putString(k, v.toString().take(100))
                    }
                }
            }
        fa.logEvent(event, bundle)
    }

    /** Standard Firebase screen_view event. Aggregates engagement time and
     *  bounce per route automatically in the Firebase console. */
    fun screenView(routeName: String) {
        log(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            FirebaseAnalytics.Param.SCREEN_NAME to routeName,
        )
    }

    /** Optional user-property tagging (e.g., "ru", a11y profile, free/paid). */
    fun setUserProperty(
        key: String,
        value: String?,
    ) {
        fa.setUserProperty(key, value?.take(36))
    }
}

/** Event names. Group by domain. Keep names stable — renames break dashboards. */
object Events {
    // App lifecycle
    const val APP_OPEN = "app_open"

    // Onboarding
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val TUTORIAL_REPLAYED = "tutorial_replayed"

    // Quiz lifecycle (mode/score/correct in params)
    const val QUIZ_STARTED = "quiz_started"
    const val QUIZ_ANSWERED = "quiz_answered"
    const val QUIZ_FINISHED = "quiz_finished"
    const val QUIZ_FORFEITED = "quiz_forfeited"
    const val QUIZ_PLAYBACK_ERROR = "quiz_playback_error"

    // Daily challenge
    const val DAILY_OPENED = "daily_opened"
    const val DAILY_STARTED = "daily_started"
    const val DAILY_REMINDER_SHOWN = "daily_reminder_shown"

    // Duel (async via Firestore)
    const val DUEL_CREATE = "duel_create"
    const val DUEL_JOIN = "duel_join"
    const val DUEL_ROUND_STARTED = "duel_round_started"
    const val DUEL_FINISHED = "duel_finished"
    const val DUEL_EXITED = "duel_exited"

    // Hints + economy
    const val HINT_USED = "hint_used"
    const val HINT_BOUGHT_COINS = "hint_bought_coins"
    const val HINT_AD_REQUESTED = "hint_ad_requested"
    const val HINT_AD_REWARDED = "hint_ad_rewarded"
    const val HINT_AD_CANCELED = "hint_ad_canceled"
    const val COINS_EARNED = "coins_earned"
    const val COINS_SPENT = "coins_spent"
    const val NEW_HIGH_SCORE = "new_high_score"
    const val STREAK_MILESTONE = "streak_milestone"

    // Leaderboard / social
    const val LEADERBOARD_LOAD = "leaderboard_load"
    const val FRIENDS_OPENED = "friends_opened"
    const val FRIEND_ADD_RESULT = "friend_add_result"
    const val FRIEND_REMOVED = "friend_removed"
    const val FRIEND_CODE_SHARED = "friend_code_shared"

    // Notifications
    const val NOTIF_PERMISSION_RESULT = "notif_permission_result"
    const val NOTIF_TRAY_TAPPED = "notif_tray_tapped"
    const val NOTIF_SETTINGS_OPENED = "notif_settings_opened"
    const val NOTIF_INBOX_OPENED = "notif_inbox_opened"

    // Settings
    const val SETTING_TOGGLED = "setting_toggled"
    const val HIGH_SCORE_RESET = "high_score_reset"

    // Sharing
    const val SHARE_RESULT = "share_result"

    // Errors
    const val ERROR = "app_error"
}

/** Parameter keys. Keep names short and consistent across events. */
object Params {
    const val MODE = "mode" // EASY | NORMAL | RANDOM | SHIT | DAILY | DUEL
    const val SCORE = "score"
    const val CORRECT_COUNT = "correct_count"
    const val TOTAL_QUESTIONS = "total_questions"
    const val DURATION_MS = "duration_ms"
    const val QUESTION_INDEX = "question_index"
    const val TIME_REMAINING_MS = "time_remaining_ms"
    const val CORRECT = "correct" // bool
    const val ENDED_VIA = "ended_via" // complete | wrong | timeout | skip | forfeit

    const val HINT_TYPE = "hint_type" // FIFTY_FIFTY | REVEAL_LETTER | SKIP
    const val PRICE = "price"
    const val COINS_DELTA = "coins_delta"
    const val COINS_SOURCE = "coins_source" // run | streak_bonus
    const val COINS_SINK = "coins_sink" // hint
    const val SOURCE = "source" // menu | result | daily | duel | shop

    const val DUEL_ROLE = "duel_role" // HOST | GUEST
    const val DUEL_OUTCOME = "duel_outcome" // win | lose | tie | unresolved
    const val DUEL_PHASE = "duel_phase" // setup | lobby | round | result
    const val RESULT = "result" // ok | offline | not_found | full | self | duplicate
    const val GRANTED = "granted" // bool

    const val DESTINATION = "destination" // INBOX | DAILY
    const val UNREAD_COUNT = "unread_count"
    const val PERMISSION_GRANTED = "permission_granted"

    const val STREAK_DAYS = "streak_days"
    const val STREAK_AT_RISK = "streak_at_risk"

    const val KEY = "key" // settings key (poster | sound | reduce_motion | …)
    const val VALUE = "value" // bool

    const val SCREEN = "screen"
    const val SHARE_KIND = "share_kind" // text | image

    const val LEADERBOARD_TOTAL = "leaderboard_total"
    const val LEADERBOARD_MY_RANK = "leaderboard_my_rank"

    const val ERROR_KIND = "error_kind"
    const val ERROR_MESSAGE = "error_message"
}
