package kz.yers.quiz.data.ads

import kz.yers.quiz.BuildConfig

/**
 * Yandex Advertising Network ad unit IDs (Partner interface → Mobile apps → ad units).
 * Debug builds always use Yandex's demo units so testing never inflates real stats.
 */
object AdUnits {
    val REWARDED: String = if (BuildConfig.DEBUG) "demo-rewarded-yandex" else "R-M-20139666-1"
    val INTERSTITIAL: String = if (BuildConfig.DEBUG) "demo-interstitial-yandex" else "R-M-20139666-2"
}
