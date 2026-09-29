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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kz.yers.quiz.R
import kz.yers.quiz.data.ads.AdsConsent
import kz.yers.quiz.data.ads.AdsManager
import kz.yers.quiz.ui.theme.QuizColors
import org.koin.compose.koinInject

internal fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * When [active] flips true, shows a rewarded ad through [AdsManager] (preloaded, so usually
 * instant). Invokes [onReward] iff the player earned the reward, otherwise [onDismiss] (closed
 * early, no fill, no consent, or no Activity). Renders a dim scrim with a spinner meanwhile.
 */
@Composable
fun RewardedAdEffect(
    active: Boolean,
    onReward: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val ads: AdsManager = koinInject()
    val currentOnReward by rememberUpdatedState(onReward)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    DisposableEffect(active) {
        if (active) {
            val activity = context.findActivity()
            if (activity == null || !AdsConsent.canRequestAds(context)) {
                Toast.makeText(context, R.string.ad_unavailable, Toast.LENGTH_SHORT).show()
                currentOnDismiss()
            } else {
                ads.showRewarded(activity) { result ->
                    when (result) {
                        AdsManager.RewardedResult.EARNED -> currentOnReward()
                        AdsManager.RewardedResult.CLOSED_EARLY -> currentOnDismiss()
                        AdsManager.RewardedResult.UNAVAILABLE -> {
                            Toast.makeText(context, R.string.ad_unavailable, Toast.LENGTH_SHORT).show()
                            currentOnDismiss()
                        }
                    }
                }
            }
        }
        onDispose { if (active) ads.cancelPendingRewarded() }
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
