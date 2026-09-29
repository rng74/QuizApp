package kz.yers.quiz.data.ads

/**
 * Pure frequency cap for between-run interstitials (no Android deps, unit-tested):
 *  - never during the player's first app launch,
 *  - only after [runsBetween] finished runs since the last full-screen ad,
 *  - and at least [minIntervalMs] after any full-screen ad (rewarded included).
 */
class InterstitialPolicy(
    private val runsBetween: Int = 3,
    private val minIntervalMs: Long = 3 * 60_000L,
    private val minLaunches: Int = 2,
) {
    private var runsSinceLast = 0
    private var lastFullScreenAtMs: Long? = null

    fun onRunFinished() {
        runsSinceLast += 1
    }

    fun shouldShow(
        nowMs: Long,
        launchCount: Int,
    ): Boolean {
        if (launchCount < minLaunches) return false
        if (runsSinceLast < runsBetween) return false
        val last = lastFullScreenAtMs ?: return true
        return nowMs - last >= minIntervalMs
    }

    fun onInterstitialShown(nowMs: Long) {
        runsSinceLast = 0
        lastFullScreenAtMs = nowMs
    }

    /** A rewarded ad the player chose to watch still counts toward the time gap. */
    fun onRewardedShown(nowMs: Long) {
        lastFullScreenAtMs = nowMs
    }
}
