package kz.yers.quiz.ui.composable

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * Shared audio host for the quiz. Composable wrapper around ExoPlayer that:
 *  - requests audio focus via Media3's built-in handling (phone calls / other media pause us)
 *  - surfaces transport errors via [onPlaybackError] so QuizScreen can decide what to do
 *    (today: let the question time out naturally — never auto-mark wrong)
 *  - pauses/releases cleanly across lifecycle events.
 *
 * Recomposing with a new [url] tears down the previous player; same-url recompositions reuse it.
 */
@Composable
fun AudioPlayer(
    url: String,
    needPlay: Boolean,
    paused: Boolean = false,
    onPlaybackReady: () -> Unit,
    onPlaybackEnded: () -> Unit,
    onPlaybackError: ((PlaybackException) -> Unit)? = null,
) {
    val context = LocalContext.current
    val exoPlayer =
        remember(url) {
            ExoPlayer.Builder(context)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_GAME)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    // handleAudioFocus =
                    true,
                )
                .build().apply {
                    val mediaItem = MediaItem.fromUri(Uri.parse(url))
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                    repeatMode = ExoPlayer.REPEAT_MODE_OFF
                }
        }
    if (!needPlay) {
        exoPlayer.stop()
    }
    LaunchedEffect(paused, needPlay) {
        if (!needPlay) return@LaunchedEffect
        if (paused) exoPlayer.pause() else exoPlayer.play()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(url) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> exoPlayer.play()
                    Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                    Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                    else -> {}
                }
            }
        val listener =
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlaybackEnded()
                    } else if (playbackState == Player.STATE_READY) {
                        onPlaybackReady()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    // A network drop / source-load failure mid-track lands here.
                    // We never auto-mark the question wrong — the timer keeps running
                    // and the player can still answer; the callback lets the host
                    // surface a hint to the user if desired.
                    onPlaybackError?.invoke(error)
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
