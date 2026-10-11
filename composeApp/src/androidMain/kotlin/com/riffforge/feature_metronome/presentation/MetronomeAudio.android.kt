package com.riffforge.feature_metronome.presentation

import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

actual fun playMetronomeClick(isAccent: Boolean, beat: Int) {
    try {
        Handler(Looper.getMainLooper()).post {
            val generator = ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
            if (isAccent) {
                // TONE_DTMF_9 = high short beep for first beat accent
                generator.startTone(ToneGenerator.TONE_DTMF_9, 30)
            } else {
                // TONE_DTMF_0 = low short click for regular beats
                generator.startTone(ToneGenerator.TONE_DTMF_0, 30)
            }
            generator.release()
        }
    } catch (_: Exception) {
        // Silently fail
    }
}
