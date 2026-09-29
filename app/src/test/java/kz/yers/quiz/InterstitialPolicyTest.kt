package kz.yers.quiz

import kz.yers.quiz.data.ads.InterstitialPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialPolicyTest {
    private fun policyAfterRuns(runs: Int) = InterstitialPolicy().apply { repeat(runs) { onRunFinished() } }

    @Test
    fun neverOnFirstLaunch() {
        assertFalse(policyAfterRuns(10).shouldShow(nowMs = 0, launchCount = 1))
    }

    @Test
    fun needsThreeRuns() {
        assertFalse(policyAfterRuns(2).shouldShow(nowMs = 0, launchCount = 2))
        assertTrue(policyAfterRuns(3).shouldShow(nowMs = 0, launchCount = 2))
    }

    @Test
    fun resetsRunCounterAndEnforcesThreeMinuteGap() {
        val p = policyAfterRuns(3)
        p.onInterstitialShown(nowMs = 1_000)
        repeat(3) { p.onRunFinished() }
        assertFalse(p.shouldShow(nowMs = 1_000 + 179_999, launchCount = 2))
        assertTrue(p.shouldShow(nowMs = 1_000 + 180_000, launchCount = 2))
    }

    @Test
    fun rewardedAdCountsTowardTimeGapButNotRuns() {
        val p = policyAfterRuns(3)
        p.onRewardedShown(nowMs = 10_000)
        assertFalse(p.shouldShow(nowMs = 60_000, launchCount = 5))
        assertTrue(p.shouldShow(nowMs = 190_000, launchCount = 5))
    }
}
