package kz.yers.quiz.ui.composable

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
import kz.yers.quiz.BuildConfig
import kz.yers.quiz.model.LocalA11y
import kz.yers.quiz.ui.theme.QuizColors

@Composable
fun BlurredImage(
    url: String,
    isBlurred: Boolean,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val reduceMotion = LocalA11y.current.reduceMotion
    // Reveal is a cheap alpha crossfade — the sharp poster fades in over the
    // statically-blurred backdrop. No animated blur RenderEffect per frame.
    val sharpAlpha by animateFloatAsState(
        targetValue = if (isBlurred) 0f else 1f,
        // Reveal fades in; re-blurring (next round, new poster) snaps instantly
        // so a fast-cached poster is never shown sharp before the round starts.
        animationSpec = if (isBlurred || reduceMotion) snap() else tween(durationMillis = 350),
        label = "posterReveal",
    )

    val request =
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build()

    Box(modifier = modifier) {
        // Cropped, statically-blurred copy fills the panel so portrait posters
        // don't leave bare side bars, and is the obscured state until reveal.
        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(24.dp),
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
        // Sharp, fitted poster fades in on reveal (Coil serves the same cached
        // bitmap, so this is one decode shared with the backdrop).
        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = sharpAlpha },
            loading = {},
            error = {},
            success = { SubcomposeAsyncImageContent() },
        )
    }
}
