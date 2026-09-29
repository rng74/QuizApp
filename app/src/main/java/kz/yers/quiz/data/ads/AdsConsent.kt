package kz.yers.quiz.data.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * GDPR / UK / Swiss consent via Google's User Messaging Platform, and the single place the ads
 * SDK is initialized. Outside regulated regions UMP resolves immediately with `canRequestAds`
 * = true, so this adds no friction for RU/CIS players.
 *
 * The consent message itself is configured in AdMob → Privacy & messaging → European
 * regulations; until one is published, UMP shows nothing and ads stay non-personalized-safe.
 */
object AdsConsent {
    private val adsInitialized = AtomicBoolean(false)

    private fun info(context: Context): ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    /** Call once per launch from the Activity. Shows the consent form if (and only if) required. */
    fun gather(activity: Activity) {
        val info = info(activity)
        info.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    initAdsIfAllowed(activity)
                }
            },
            { initAdsIfAllowed(activity) },
        )
        // Consent from a previous session is already cached — don't wait for the network.
        initAdsIfAllowed(activity)
    }

    fun canRequestAds(context: Context): Boolean = info(context).canRequestAds()

    /** True when the player must be offered a way to change their choice (Settings entry). */
    fun privacyOptionsRequired(context: Context): Boolean =
        info(context).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { initAdsIfAllowed(activity) }
    }

    private fun initAdsIfAllowed(context: Context) {
        if (!canRequestAds(context)) return
        if (adsInitialized.compareAndSet(false, true)) {
            MobileAds.initialize(context.applicationContext)
        }
    }
}
