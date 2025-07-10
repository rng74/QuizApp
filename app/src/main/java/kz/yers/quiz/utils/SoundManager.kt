package kz.yers.quiz.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kz.yers.quiz.R
import java.lang.Math.random

object SoundManager {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var loaded = false

    fun init(context: Context) {
        val audioAttributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        soundPool =
            SoundPool
                .Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build()

        soundId = soundPool!!.load(context, R.raw.correct, 1)

        soundPool!!.setOnLoadCompleteListener { _, _, status ->
            loaded = status == 0
        }
    }

    fun playCorrectAnswer() {
        if (loaded) {
            // Add a slight pitch variation for dopamine boost
            val pitch = (0.95f + random() * 0.1f).toFloat() // Random pitch between 0.95 and 1.05
            soundPool?.play(soundId, 1f, 1f, 1, 0, pitch)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
