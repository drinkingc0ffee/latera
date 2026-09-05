package com.c0ffeebreak.latera

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var beatsPerSecond: Float
        get() = prefs.getFloat(KEY_BPS, DEFAULT_BPS).coerceIn(MIN_BPS, MAX_BPS)
        set(value) {
            prefs.edit().putFloat(KEY_BPS, value.coerceIn(MIN_BPS, MAX_BPS)).apply()
        }

    var darkBackground: Boolean
        get() = prefs.getBoolean(KEY_DARK_BG, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_BG, value).apply()
        }

    var ballSize: Float
        get() = prefs.getFloat(KEY_BALL_SIZE, DEFAULT_BALL_SIZE).coerceIn(MIN_BALL_SIZE, MAX_BALL_SIZE)
        set(value) {
            prefs.edit().putFloat(KEY_BALL_SIZE, value.coerceIn(MIN_BALL_SIZE, MAX_BALL_SIZE)).apply()
        }

    var audioEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUDIO, true)
        set(value) {
            prefs.edit().putBoolean(KEY_AUDIO, value).apply()
        }

    companion object {
        const val PREFS_NAME = "latera"
        const val KEY_BPS = "beats_per_second"
        const val KEY_DARK_BG = "dark_background"
        const val KEY_BALL_SIZE = "ball_size"
        const val KEY_AUDIO = "audio_enabled"
        const val DEFAULT_BPS = 1.0f
        const val MIN_BPS = 0.25f
        const val MAX_BPS = 4.0f
        const val DEFAULT_BALL_SIZE = 0.22f
        const val MIN_BALL_SIZE = 0.08f
        const val MAX_BALL_SIZE = 0.45f
    }
}
