package kz.yers.quiz.ui.composable

import android.os.Build
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Size
import kz.yers.quiz.BuildConfig
import kz.yers.quiz.model.LocalA11y
import kz.yers.quiz.ui.theme.QuizColors

/**
 * Two-layer poster reveal:
 *   1. Backdrop — cropped, obscured copy of the poster (fills the panel).
 *   2. Foreground — sharp, fitted poster; alpha = 0 until reveal, then fades in.
 *
 * Backdrop obscuration:
 *  - API 31+ (Android 12+): `Modifier.blur(24.dp)` does a real GPU blur — looks great.
 *  - API < 31: `Modifier.blur` is silently a no-op (the spoiler-protection that A03
 *    users were hitting). Fall back to a 32×32 Coil decode and let `ContentScale.Crop`
 *    stretch those pixels across the panel — heavy pixelation, no detail leak, no
 *    extra dependency. Same URL, separate cache entry from the front-layer full-res
 *    request so neither path pollutes the other.
 */
@Composable
fun BlurredImage(
    url: String,
    isBlurred: Boolean,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val reduceMotion = LocalA11y.current.reduceMotion
    val supportsRenderBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val sharpAlpha by animateFloatAsState(
        targetValue = if (isBlurred) 0f else 1f,
        animationSpec = if (isBlurred || reduceMotion) snap() else tween(durationMillis = 350),
        label = "posterReveal",
    )

    val sharpRequest =
        remember(url) {
            ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .build()
        }
    // Back layer: full-size on devices with real blur support; tiny pixelated decode
    // on older devices. `Size(32, 32)` is the magic number — anything ≥ 64 starts
    // revealing recognisable shapes, anything < 16 reads as a solid wash.
    val obscuredRequest =
        remember(url, supportsRenderBlur) {
            if (supportsRenderBlur) {
                sharpRequest
            } else {
                ImageRequest.Builder(context)
                    .data(url)
                    .size(Size(32, 32))
                    .crossfade(false)
                    .build()
            }
        }

    Box(modifier = modifier) {
        SubcomposeAsyncImage(
            model = obscuredRequest,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(if (supportsRenderBlur) Modifier.blur(24.dp) else Modifier),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize().background(QuizColors.posterPlaceholder),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            },
            error = {
                if (BuildConfig.DEBUG) {
                    Log.e(
                        "BlurredImage",
                        "Failed to load $url: ${it.result.throwable.message}",
                        it.result.throwable,
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize().background(QuizColors.posterError),
                )
            },
            success = { SubcomposeAsyncImageContent() },
        )
        SubcomposeAsyncImage(
            model = sharpRequest,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = sharpAlpha },
            loading = {},
            error = {},
            success = { SubcomposeAsyncImageContent() },
        )
    }
}
