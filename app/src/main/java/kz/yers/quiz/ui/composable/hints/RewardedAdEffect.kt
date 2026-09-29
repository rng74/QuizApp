package kz.yers.quiz.ui.composable.hints

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kz.yers.quiz.R
import kz.yers.quiz.data.ads.AdsConsent
import kz.yers.quiz.ui.theme.QuizColors

// Production rewarded ad unit (matches the AdMob app id in AndroidManifest.xml).
// Newly-created AdMob units take 24–48 h before Google's inventory serves real
// ads consistently; until then expect no-fill / test ads.
private const val REWARDED_AD_UNIT = "ca-app-pub-8999744317337712/1049947691"

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * When [active] flips true, loads and shows a real AdMob rewarded ad. Invokes [onReward] iff the
 * user earned the reward, otherwise [onDismiss] (dismissed early, load/show failure, or no
 * Activity). Renders a dim scrim with a spinner while the ad is loading.
 */
@Composable
fun RewardedAdEffect(
    active: Boolean,
    onReward: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnReward by rememberUpdatedState(onReward)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(active) {
        if (!active) return@LaunchedEffect
        val activity = context.findActivity()
        if (activity == null) {
            currentOnDismiss()
            return@LaunchedEffect
        }
        if (!AdsConsent.canRequestAds(context)) {
            Toast.makeText(context, R.string.ad_unavailable, Toast.LENGTH_SHORT).show()
            currentOnDismiss()
            return@LaunchedEffect
        }
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // No-fill is the norm in regions Google Ads doesn't serve (e.g. RU) —
                    // say so instead of silently dropping the spinner.
                    Toast.makeText(context, R.string.ad_unavailable, Toast.LENGTH_SHORT).show()
                    currentOnDismiss()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    var earned = false
                    ad.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                if (earned) currentOnReward() else currentOnDismiss()
                            }

                            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                                currentOnDismiss()
                            }
                        }
                    ad.show(activity) { earned = true }
                }
            },
        )
    }

    if (active) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = QuizColors.tint, strokeWidth = 3.dp)
        }
    }
}
