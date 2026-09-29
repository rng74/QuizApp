package kz.yers.quiz.data.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * GDPR / UK / Swiss consent via Google's User Messaging Platform. UMP stores IAB TCF v2 strings
 * that the Yandex SDK (and the networks it mediates) read automatically. Outside regulated
 * regions UMP resolves immediately with `canRequestAds` = true — no friction for RU/CIS players.
 *
 * The consent message itself is configured in AdMob → Privacy & messaging → European
 * regulations; until one is published, UMP shows nothing.
 */
object AdsConsent {
    private val adsStarted = AtomicBoolean(false)
    private var onAdsAllowed: (() -> Unit)? = null

    private fun info(context: Context): ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    /**
     * Call once per launch from the Activity. Shows the consent form if (and only if) required,
     * then invokes [startAds] once — as soon as consent allows requesting ads.
     */
    fun gather(
        activity: Activity,
        startAds: () -> Unit,
    ) {
        onAdsAllowed = startAds
        val info = info(activity)
        info.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    startAdsIfAllowed(activity)
                }
            },
            { startAdsIfAllowed(activity) },
        )
        // Consent from a previous session is already cached — don't wait for the network.
        startAdsIfAllowed(activity)
    }

    fun canRequestAds(context: Context): Boolean = info(context).canRequestAds()

    /** True when the player must be offered a way to change their choice (Settings entry). */
    fun privacyOptionsRequired(context: Context): Boolean =
        info(context).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { startAdsIfAllowed(activity) }
    }

    private fun startAdsIfAllowed(context: Context) {
        if (!canRequestAds(context)) return
        if (adsStarted.compareAndSet(false, true)) {
            onAdsAllowed?.invoke()
        }
    }
}
