package kz.yers.quiz.data.diagnostics

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kz.yers.quiz.data.analytics.Analytics
import kz.yers.quiz.data.analytics.Events
import kz.yers.quiz.data.analytics.Params

/**
 * Surfaces the *real* reason a previous process died.
 *
 * The Android low-memory killer, ANR killer, and forced-stop pathways do not go
 * through `Thread.UncaughtExceptionHandler`, so Crashlytics never sees them —
 * the app simply stops running. That's exactly the "app quits after a while"
 * pattern we see on low-RAM devices like the Samsung A03 (2 GB, Android 11).
 *
 * [ApplicationExitInfo] (API 30+) gives us the kernel's verdict — REASON_LOW_MEMORY,
 * REASON_ANR, REASON_CRASH, REASON_EXIT_SELF, etc. — for the last few process
 * deaths. We replay anything we haven't already reported into Crashlytics as a
 * non-fatal + into Analytics as a `app_error` event, so the next launch with
 * connectivity surfaces the cause.
 *
 * Idempotent across launches via a SharedPreferences cursor — each exit is
 * reported exactly once.
 */
object ExitReasonReporter {
    private const val PREFS = "exit_reporter"
    private const val KEY_LAST_TS = "lastReportedTimestamp"
    private const val MAX_HISTORY = 10

    /**
     * Cheap. Safe to call from MainActivity.onCreate on every launch.
     * Returns the number of new exits that were reported (useful for tests).
     */
    fun report(
        context: Context,
        analytics: Analytics,
    ): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return 0
        val am = context.getSystemService(ActivityManager::class.java) ?: return 0
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastReportedTs = prefs.getLong(KEY_LAST_TS, 0L)

        val exits =
            runCatching {
                am.getHistoricalProcessExitReasons(context.packageName, 0, MAX_HISTORY)
            }.getOrElse { return 0 }

        // Newest first. Filter to ones we haven't already logged.
        val fresh = exits.filter { it.timestamp > lastReportedTs }
        if (fresh.isEmpty()) return 0

        val crashlytics = FirebaseCrashlytics.getInstance()
        var reported = 0
        // Iterate oldest → newest so the Crashlytics order matches reality.
        for (info in fresh.reversed()) {
            val reasonName = info.reason.toReasonName()
            val importance = info.importance.toImportanceName()
            val message =
                buildString {
                    append("exit reason=").append(reasonName)
                    append(" status=").append(info.status)
                    append(" importance=").append(importance)
                    append(" pss_kb=").append(info.pss)
                    append(" rss_kb=").append(info.rss)
                    info.description?.let { append(" desc=").append(it) }
                }
            crashlytics.log(message)
            analytics.log(
                Events.ERROR,
                Params.ERROR_KIND to "process_exit_$reasonName",
                Params.ERROR_MESSAGE to (info.description ?: reasonName),
            )
            // Only file a non-fatal for the interesting kinds — REASON_EXIT_SELF +
            // REASON_USER_REQUESTED + REASON_USER_STOPPED are normal shutdowns and
            // would drown the Crashlytics inbox.
            if (info.reason in NOISY_OK_REASONS) continue
            crashlytics.recordException(
                RuntimeException("Prior process exit: $reasonName — $message"),
            )
            reported += 1
        }

        prefs.edit { putLong(KEY_LAST_TS, exits.first().timestamp) }
        return reported
    }

    private val NOISY_OK_REASONS =
        setOf(
            ApplicationExitInfo.REASON_USER_REQUESTED,
            ApplicationExitInfo.REASON_USER_STOPPED,
            ApplicationExitInfo.REASON_EXIT_SELF,
            ApplicationExitInfo.REASON_DEPENDENCY_DIED,
            ApplicationExitInfo.REASON_OTHER,
        )

    private fun Int.toReasonName(): String =
        when (this) {
            ApplicationExitInfo.REASON_UNKNOWN -> "unknown"
            ApplicationExitInfo.REASON_EXIT_SELF -> "exit_self"
            ApplicationExitInfo.REASON_SIGNALED -> "signaled"
            ApplicationExitInfo.REASON_LOW_MEMORY -> "low_memory"
            ApplicationExitInfo.REASON_CRASH -> "crash"
            ApplicationExitInfo.REASON_CRASH_NATIVE -> "crash_native"
            ApplicationExitInfo.REASON_ANR -> "anr"
            ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "init_failure"
            ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "permission_change"
            ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "excessive_resource"
            ApplicationExitInfo.REASON_USER_REQUESTED -> "user_requested"
            ApplicationExitInfo.REASON_USER_STOPPED -> "user_stopped"
            ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "dependency_died"
            ApplicationExitInfo.REASON_OTHER -> "other"
            else -> "reason_$this"
        }

    private fun Int.toImportanceName(): String =
        when (this) {
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "foreground"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE -> "foreground_service"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> "visible"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE -> "perceptible"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE -> "service"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED -> "cached"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE -> "gone"
            else -> "importance_$this"
        }
}
