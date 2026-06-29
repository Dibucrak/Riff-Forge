package com.riffforge.feature_ear_training.domain.util

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.sin

object AudioSynth {

    private fun playTone(freqOfTone: Double, durationMs: Int) {
        val sampleRate = 44100
        val numSamples = (durationMs * sampleRate) / 1000
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)

        for (i in 0 until numSamples) {
            sample[i] = sin(2 * Math.PI * i / (sampleRate / freqOfTone))
        }

        var idx = 0
        for (dVal in sample) {
            val valShort = (dVal * 32767).toInt().toShort()
            generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = (valShort.toInt() and 0xff00 ushr 8).toByte()
        }

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, generatedSnd.size,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()

        Thread.sleep(durationMs.toLong() + 100)
        audioTrack.release()
    }

    private val C4 = 261.63
    private val E4 = 329.63
    private val G4 = 392.00
    private val A4 = 440.00

    fun playSoundForQuestion(answer: String) {
        Thread {
            when (answer) {
                "Mayor" -> { playTone(C4, 400); playTone(E4, 400); playTone(G4, 600) }
                "Quinta Justa" -> { playTone(C4, 500); playTone(G4, 500) }
                "Menor 7" -> { playTone(A4, 400); playTone(C4, 400); playTone(E4, 400); playTone(392.00, 600) }
                "Tercera Mayor" -> { playTone(C4, 500); playTone(E4, 500) }
                else -> { playTone(440.0, 300) }
            }
        }.start()
    }
}