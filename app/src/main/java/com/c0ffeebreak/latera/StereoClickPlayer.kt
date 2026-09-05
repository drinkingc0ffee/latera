package com.c0ffeebreak.latera

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

/**
 * Plays CC0 metronome samples: a mechanical tick on the left,
 * an electric tock on the right.
 */
class StereoClickPlayer(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val soundPool: SoundPool
    private val tickId: Int
    private val tockId: Int
    private var loaded = 0
    private var focusRequest: AudioFocusRequest? = null

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded += 1
        }
        tickId = soundPool.load(context, R.raw.tick, 1)
        tockId = soundPool.load(context, R.raw.tock, 1)
        requestFocus(attrs)
    }

    fun playLeft() {
        if (loaded < 2) return
        soundPool.play(tickId, 1f, 0f, 1, 0, 1f)
    }

    fun playRight() {
        if (loaded < 2) return
        soundPool.play(tockId, 0f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun requestFocus(attrs: AudioAttributes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setAcceptsDelayedFocusGain(false)
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
    }
}
