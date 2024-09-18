package kz.yers.quiz.ui.composable

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun BlurredImage(
    url: String,
    isBlurred: Boolean,
    modifier: Modifier
) {
    val context = LocalContext.current
    val blurRadius by animateDpAsState(targetValue = if (isBlurred) 16.dp else 0.dp, label = "")

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier.blur(blurRadius)
    )
}