package com.riffforge.feature_tuner.domain

import kotlinx.coroutines.flow.Flow
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PitchResult(
    val frequency: Float,
    val noteName: String,
    val octave: Int,
    val cents: Float
)

object PitchDetectionMath {

    private val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    /**
     * Autocorrelation pitch detection on float PCM samples [-1f, 1f].
     * Returns 0f if signal energy is below noise gate or pitch is out of guitar range.
     */
    fun detectFrequency(samples: FloatArray, sampleRate: Int): Float {
        if (samples.size < 256) return 0f

        // Noise gate (RMS check)
        var sumSq = 0.0
        for (s in samples) {
            sumSq += s * s
        }
        val rms = sqrt(sumSq / samples.size)
        if (rms < 0.008) return 0f

        // Guitar range ~65 Hz (C2) to ~1100 Hz (C6)
        val minPeriod = (sampleRate / 1100).coerceAtLeast(16)
        val maxPeriod = (sampleRate / 65).coerceAtMost(samples.size / 2 - 2)
        if (maxPeriod <= minPeriod) return 0f

        val correlations = DoubleArray(maxPeriod + 2)
        var maxCorrelation = 0.0
        var bestPeriod = -1

        for (period in minPeriod..maxPeriod) {
            var correlation = 0.0
            val limit = samples.size - period
            for (i in 0 until limit) {
                correlation += samples[i] * samples[i + period]
            }
            correlations[period] = correlation
            if (correlation > maxCorrelation) {
                maxCorrelation = correlation
                bestPeriod = period
            }
        }

        if (bestPeriod <= minPeriod || bestPeriod >= maxPeriod) {
            return if (bestPeriod > 0) sampleRate.toFloat() / bestPeriod else 0f
        }

        val y0 = correlations[bestPeriod - 1]
        val y1 = correlations[bestPeriod]
        val y2 = correlations[bestPeriod + 1]
        val denom = 2.0 * (2.0 * y1 - y0 - y2)
        val delta = if (kotlin.math.abs(denom) > 1e-9) ((y0 - y2) / denom).coerceIn(-0.5, 0.5) else 0.0
        val refinedPeriod = bestPeriod + delta
        return (sampleRate / refinedPeriod).toFloat()
    }

    fun analyzeFrequency(frequency: Float, targetFrequency: Float? = null): PitchResult? {
        if (frequency !in 60f..1200f) return null

        val a4 = 440.0
        val noteNumber = (12.0 * log2(frequency / a4) + 69.0).roundToInt()
        val exactFrequency = if (targetFrequency != null && targetFrequency > 0f) {
            targetFrequency.toDouble()
        } else {
            a4 * exp(((noteNumber - 69.0) / 12.0) * ln(2.0))
        }

        val cents = (1200.0 * log2(frequency / exactFrequency)).toFloat().coerceIn(-50f, 50f)

        var index = noteNumber % 12
        if (index < 0) index += 12
        val octave = (noteNumber / 12) - 1
        val noteName = noteNames[index]

        return PitchResult(
            frequency = frequency,
            noteName = noteName,
            octave = octave,
            cents = cents
        )
    }
}

expect fun observeMicrophonePitch(targetFrequencyProvider: () -> Float?): Flow<PitchResult?>
