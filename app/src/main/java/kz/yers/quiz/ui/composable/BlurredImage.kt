package kz.yers.quiz.ui.composable

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest

@Composable
fun BlurredImage(
    url: String,
    isBlurred: Boolean,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val blurRadius by animateDpAsState(targetValue = if (isBlurred) 16.dp else 0.dp, label = "")

    Box(modifier = modifier) {
        SubcomposeAsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(blurRadius),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF1F1F1F)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            },
            error = {
                Log.e(
                    "BlurredImage",
                    "Failed to load $url: ${it.result.throwable.message}",
                    it.result.throwable,
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF3A1F1F)),
                )
            },
            success = {
                SubcomposeAsyncImageContent()
            },
        )
    }
}
