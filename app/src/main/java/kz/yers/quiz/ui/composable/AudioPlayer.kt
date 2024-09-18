package kz.yers.quiz.ui.composable

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun AudioPlayer(url: String, onPlaybackReady: () -> Unit, onPlaybackEnded: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_OFF
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(url) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> exoPlayer.play()
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                else -> {}
            }
        }
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED)
                    onPlaybackEnded()
                else if (playbackState == Player.STATE_READY)
                    onPlaybackReady()
            }
        }
        exoPlayer.addListener(listener)
        lifecycle.addObserver(observer)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
            lifecycle.removeObserver(observer)
        }
    }
}