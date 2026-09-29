package kz.yers.quiz.data.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.core.content.edit
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.YandexAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import kz.yers.quiz.BuildConfig
import kz.yers.quiz.data.analytics.Analytics
import kz.yers.quiz.data.analytics.Events
import kz.yers.quiz.data.analytics.Params

/**
 * Single owner of the ad stack: Yandex Mobile Ads SDK with Yandex mediation (Google/AdMob demand
 * arrives through the mediation adapter — the app never calls Google's ads SDK directly).
 *
 * One rewarded and one interstitial ad are kept preloaded so a tap shows an ad immediately
 * instead of a spinner. All calls happen on the main thread (SDK callbacks are main-thread too).
 */
class AdsManager(
    context: Context,
    private val analytics: Analytics,
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("ads_prefs", Context.MODE_PRIVATE)
    private val policy = InterstitialPolicy()

    private var initialized = false
    private var rewardedLoader: RewardedAdLoader? = null
    private var interstitialLoader: InterstitialAdLoader? = null
    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null
    private var rewardedLoading = false
    private var interstitialLoading = false
    private var interstitialShowing = false

    /** Callback for a rewarded request that arrived before an ad was ready. */
    private var pendingRewardedShow: (() -> Unit)? = null

    private var launchCount: Int = prefs.getInt(KEY_LAUNCHES, 0)

    /** Call once per cold start (not on rotation). */
    fun onAppLaunch() {
        launchCount += 1
        prefs.edit { putInt(KEY_LAUNCHES, launchCount) }
    }

    /**
     * Initializes the SDK. Must only be called once consent allows ads ([AdsConsent]); the UMP
     * TCF strings it stores are read by the Yandex SDK and passed on to mediated networks.
     */
    fun initialize() {
        if (initialized) return
        initialized = true
        if (BuildConfig.DEBUG) YandexAds.enableLogging(true)
        YandexAds.initialize(appContext) {
            rewardedLoader = RewardedAdLoader(appContext)
            interstitialLoader = InterstitialAdLoader(appContext)
            loadRewarded()
            loadInterstitial()
        }
    }

    // region Rewarded

    private fun loadRewarded() {
        val loader = rewardedLoader ?: return
        if (rewarded != null || rewardedLoading) return
        rewardedLoading = true
        loader.loadAd(
            AdRequest.Builder(AdUnits.REWARDED).build(),
            object : RewardedAdLoadListener {
                override fun onAdLoaded(rewarded: RewardedAd) {
                    rewardedLoading = false
                    this@AdsManager.rewarded = rewarded
                    pendingRewardedShow?.let {
                        pendingRewardedShow = null
                        it()
                    }
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    rewardedLoading = false
                    logLoadFailure("rewarded", error)
                    pendingRewardedShow?.let {
                        pendingRewardedShow = null
                        it()
                    }
                }
            },
        )
    }

    enum class RewardedResult { EARNED, CLOSED_EARLY, UNAVAILABLE }

    /**
     * Shows a rewarded ad; [onResult] is invoked exactly once. If nothing is preloaded yet,
     * waits for the in-flight load (the caller shows a spinner meanwhile).
     */
    fun showRewarded(
        activity: Activity,
        onResult: (RewardedResult) -> Unit,
    ) {
        val ad = rewarded
        if (ad == null) {
            if (!initialized || rewardedLoader == null) {
                onResult(RewardedResult.UNAVAILABLE)
                return
            }
            pendingRewardedShow = {
                val loaded = rewarded
                if (loaded == null) onResult(RewardedResult.UNAVAILABLE) else present(activity, loaded, onResult)
            }
            loadRewarded()
            return
        }
        present(activity, ad, onResult)
    }

    /** Drops a waiting [showRewarded] request (e.g. the screen went away). */
    fun cancelPendingRewarded() {
        pendingRewardedShow = null
    }

    private fun present(
        activity: Activity,
        ad: RewardedAd,
        onResult: (RewardedResult) -> Unit,
    ) {
        rewarded = null
        var earned = false
        var delivered = false
        val finish = { value: RewardedResult ->
            if (!delivered) {
                delivered = true
                onResult(value)
            }
            loadRewarded()
        }
        ad.setAdEventListener(
            object : RewardedAdEventListener {
                override fun onAdShown() {
                    policy.onRewardedShown(SystemClock.elapsedRealtime())
                }

                override fun onAdFailedToShow(adError: AdError) {
                    finish(RewardedResult.UNAVAILABLE)
                }

                override fun onAdDismissed() {
                    finish(if (earned) RewardedResult.EARNED else RewardedResult.CLOSED_EARLY)
                }

                override fun onAdClicked() = Unit

                override fun onAdImpression(impressionData: ImpressionData?) {
                    analytics.log(Events.AD_IMPRESSION, Params.AD_FORMAT to "rewarded")
                }

                override fun onRewarded(reward: Reward) {
                    earned = true
                }
            },
        )
        ad.show(activity)
    }

    // endregion

    // region Interstitial

    private fun loadInterstitial() {
        val loader = interstitialLoader ?: return
        if (interstitial != null || interstitialLoading) return
        interstitialLoading = true
        loader.loadAd(
            AdRequest.Builder(AdUnits.INTERSTITIAL).build(),
            object : InterstitialAdLoadListener {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialLoading = false
                    interstitial = interstitialAd
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    interstitialLoading = false
                    logLoadFailure("interstitial", error)
                }
            },
        )
    }

    /** Solo run finished — counts toward the interstitial cadence. */
    fun onRunFinished() {
        policy.onRunFinished()
    }

    /**
     * Shows a between-runs interstitial if the frequency cap allows and one is loaded, then calls
     * [onDone] (always, exactly once — immediately when no ad is shown).
     */
    fun maybeShowInterstitial(
        activity: Activity,
        onDone: () -> Unit,
    ) {
        // A second tap while the ad is opening must not navigate underneath it (and then again
        // on dismiss) — drop it; the first call's onDone will fire.
        if (interstitialShowing) return
        val now = SystemClock.elapsedRealtime()
        val ad = interstitial
        if (ad == null || !policy.shouldShow(now, launchCount)) {
            loadInterstitial()
            onDone()
            return
        }
        interstitial = null
        interstitialShowing = true
        var delivered = false
        val finish = {
            interstitialShowing = false
            if (!delivered) {
                delivered = true
                onDone()
            }
            loadInterstitial()
        }
        ad.setAdEventListener(
            object : InterstitialAdEventListener {
                override fun onAdShown() {
                    policy.onInterstitialShown(SystemClock.elapsedRealtime())
                }

                override fun onAdFailedToShow(adError: AdError) = finish()

                override fun onAdDismissed() = finish()

                override fun onAdClicked() = Unit

                override fun onAdImpression(impressionData: ImpressionData?) {
                    analytics.log(Events.AD_IMPRESSION, Params.AD_FORMAT to "interstitial")
                }
            },
        )
        ad.show(activity)
    }

    // endregion

    private fun logLoadFailure(
        format: String,
        error: AdRequestError,
    ) {
        analytics.log(
            Events.AD_LOAD_FAILED,
            Params.AD_FORMAT to format,
            Params.ERROR_KIND to error.code.toString(),
            Params.ERROR_MESSAGE to error.description,
        )
    }

    private companion object {
        const val KEY_LAUNCHES = "launchCount"
    }
}
