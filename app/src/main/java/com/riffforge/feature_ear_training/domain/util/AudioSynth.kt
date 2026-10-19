package com.riffforge.feature_ear_training.domain.util

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.exp
import kotlin.math.sin

/**
 * AudioSynth — Generates realistic strummed chord sounds using multiple simultaneous
 * oscillators with envelope (ADSR-like) for each note, producing a guitar-like timbre
 * instead of simple sine wave bips.
 */
object AudioSynth {

    private const val SAMPLE_RATE = 44100
    private const val CHORD_DURATION_MS = 1200

    /**
     * Play a strummed chord by combining multiple frequencies simultaneously
     * with a decay envelope to simulate a guitar strum.
     */
    private fun playChord(frequencies: List<Double>, durationMs: Int = CHORD_DURATION_MS) {
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            var sample = 0.0
            val time = i.toDouble() / SAMPLE_RATE

            // ADSR-like envelope: fast attack, exponential decay
            val attackTime = 0.01 // 10ms attack
            val envelope = when {
                time < attackTime -> time / attackTime
                else -> exp(-3.0 * (time - attackTime))
            }

            // Sum all frequencies with slight strum delay per string
            frequencies.forEachIndexed { index, freq ->
                val strumDelay = index * 0.015 // 15ms strum delay between strings
                val strumTime = time - strumDelay
                if (strumTime > 0) {
                    val strumEnvelope = when {
                        strumTime < attackTime -> strumTime / attackTime
                        else -> exp(-3.0 * (strumTime - attackTime))
                    }
                    // Fundamental + 2 harmonics for richer timbre
                    sample += (sin(2 * Math.PI * freq * strumTime) * 0.5 +
                               sin(2 * Math.PI * freq * 2 * strumTime) * 0.2 +
                               sin(2 * Math.PI * freq * 3 * strumTime) * 0.1) * strumEnvelope
                }
            }

            // Normalize by number of frequencies and apply envelope
            sample = sample / frequencies.size * envelope
            // Clamp to [-1, 1]
            sample = sample.coerceIn(-1.0, 1.0)
            generatedSnd[i] = (sample * 16000).toInt().toShort()
        }

        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, generatedSnd.size,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()

        Thread.sleep(durationMs.toLong() + 100)
        audioTrack.release()
    }

    // Note frequencies (4th octave)
    private val C4 = 261.63
    private val D4 = 293.66
    private val Ds4 = 311.13
    private val E4 = 329.63
    private val F4 = 349.23
    private val Fs4 = 369.99
    private val G4 = 392.00
    private val Gs4 = 415.30
    private val A4 = 440.00
    private val As4 = 466.16
    private val B4 = 493.88

    // Lower octave roots
    private val C3 = 130.81
    private val D3 = 146.83
    private val Ds3 = 155.56
    private val E3 = 164.81
    private val F3 = 174.61
    private val Fs3 = 185.00
    private val G3 = 196.00
    private val Gs3 = 207.65
    private val A3 = 220.00
    private val As3 = 233.08
    private val B3 = 246.94

    fun playSoundForQuestion(answer: String) {
        Thread {
            when (answer) {
                // Major chord: root + major 3rd + perfect 5th
                "Mayor" -> playChord(listOf(C3, C4, E4, G4))
                "Menor" -> playChord(listOf(A3, A4, C4, E4))
                "Aumentado" -> playChord(listOf(C3, C4, E4, Gs4))
                "Disminuido" -> playChord(listOf(B3, B4, D4, F4))
                "Dominante 7" -> playChord(listOf(G3, G4, B4, D4, F4))
                "Mayor 7" -> playChord(listOf(C3, C4, E4, G4, B4))
                "Menor 7" -> playChord(listOf(A3, A4, C4, E4, G4))
                "Semidisminuido" -> playChord(listOf(B3, B4, D4, F4, As4))
                // Intervals
                "Quinta Justa" -> playChord(listOf(C3, C4, G4))
                "Tercera Mayor" -> playChord(listOf(C3, C4, E4))
                "Tercera Menor" -> playChord(listOf(A3, A4, C4))
                "Octava" -> playChord(listOf(C3, C4))
                "Cuarta Justa" -> playChord(listOf(C3, C4, F4))
                "Tritono" -> playChord(listOf(C3, C4, Fs4))
                "Segunda Mayor" -> playChord(listOf(C3, C4, D4))
                else -> playChord(listOf(C3, C4, E4, G4))
            }
        }.start()
    }
}