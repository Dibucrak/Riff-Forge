package com.riffforge.feature_ear_training.domain.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * AudioSynth — Karplus-Strong physical plucked acoustic guitar string synthesizer
 * with fractional delay tuning, pick-position comb filtering, wooden body warmth,
 * chord strumming, and melodic+harmonic interval playback.
 */
object AudioSynth {

    internal const val SAMPLE_RATE = 44100
    private const val CHORD_DURATION_MS = 1650
    private const val INTERVAL_DURATION_MS = 1900

    private val activeTrackRef = AtomicReference<AudioTrack?>(null)

    fun playReferenceTone(frequencyHz: Double, durationMs: Int = 750) {
        if (frequencyHz <= 0.0) return
        Thread {
            val pcm = generateStrummedPcm(
                frequencies = listOf(frequencyHz),
                durationMs = durationMs,
                strumDelaySeconds = 0.0
            )
            playPcm(pcm)
        }.start()
    }

    internal fun synthesizePluckedString(
        freqHz: Double,
        numSamples: Int,
        seedOffset: Int = 0
    ): DoubleArray {
        val out = DoubleArray(numSamples)
        if (freqHz <= 20.0 || numSamples <= 0) return out

        val period = SAMPLE_RATE.toDouble() / freqHz
        val delayLen = period.toInt().coerceIn(2, SAMPLE_RATE / 20)
        val frac = (period - delayLen).coerceIn(0.0, 1.0)
        val ring = DoubleArray(delayLen + 2)

        // Deterministic xorshift32 noise
        var rng = 0x13572468 xor ((freqHz * 100.0).toInt() + seedOffset * 977)
        if (rng == 0) rng = 0x24681357
        fun nextSignedNoise(): Double {
            var x = rng
            x = x xor (x shl 13)
            x = x xor (x ushr 17)
            x = x xor (x shl 5)
            rng = x
            return ((x ushr 1).toDouble() / Int.MAX_VALUE.toDouble()) * 2.0 - 1.0
        }

        // Low-pass smoothed noise + slight fundamental seed for rich bass body
        val rawBurst = DoubleArray(delayLen)
        var burstLp = 0.0
        for (i in 0 until delayLen) {
            burstLp = burstLp * 0.55 + nextSignedNoise() * 0.45
            val fundamentalSeed = 0.18 * sin(2.0 * PI * i.toDouble() / period)
            rawBurst[i] = burstLp + fundamentalSeed
        }

        // Pick-position comb filter (~22% of string length) removes harsh brittle harmonics
        val pickDelay = (delayLen * 0.22).toInt().coerceAtLeast(1)
        for (i in 0 until delayLen) {
            val delayed = if (i >= pickDelay) rawBurst[i - pickDelay] else 0.0
            ring[i] = rawBurst[i] - 0.68 * delayed
        }
        ring[delayLen] = ring[0]
        ring[delayLen + 1] = ring[1]

        // Frequency-aware string damping (bass sustains longer than treble)
        val damp = 0.9965 - ((freqHz - 80.0).coerceIn(0.0, 700.0) / 700.0) * 0.0045
        val bufSize = delayLen + 1
        var writeIdx = 0
        var prevFiltered = 0.0
        var bodyState = 0.0

        val attackSamples = (0.003 * SAMPLE_RATE).toInt().coerceAtLeast(1)
        val releaseSamples = (0.050 * SAMPLE_RATE).toInt().coerceAtLeast(1)

        for (i in 0 until numSamples) {
            val readBase = (writeIdx + 1) % bufSize
            val readNext = (readBase + 1) % bufSize
            val interpolated = ring[readBase] * (1.0 - frac) + ring[readNext] * frac

            // Two-point Karplus-Strong string low-pass
            val ks = 0.5 * (interpolated + prevFiltered) * damp
            prevFiltered = interpolated
            ring[writeIdx] = ks
            writeIdx = (writeIdx + 1) % bufSize

            // Gentle acoustic soundboard warmth filter
            bodyState = bodyState * 0.30 + ks * 0.70
            val sample = 0.78 * ks + 0.22 * bodyState

            val attackEnv = if (i < attackSamples) 0.5 - 0.5 * cos(PI * i / attackSamples) else 1.0
            val releaseEnv = if (i >= numSamples - releaseSamples) {
                val rem = (numSamples - 1 - i).coerceAtLeast(0)
                0.5 - 0.5 * cos(PI * rem / releaseSamples)
            } else 1.0
            out[i] = sample * attackEnv * releaseEnv
        }
        return out
    }

    internal fun generateStrummedPcm(
        frequencies: List<Double>,
        durationMs: Int = CHORD_DURATION_MS,
        strumDelaySeconds: Double = 0.024
    ): ShortArray {
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val out = ShortArray(numSamples)
        if (frequencies.isEmpty() || numSamples <= 0) return out

        val mix = DoubleArray(numSamples)
        frequencies.forEachIndexed { index, freq ->
            val offsetSamples = (index * strumDelaySeconds * SAMPLE_RATE).toInt().coerceAtLeast(0)
            val strLen = (numSamples - offsetSamples).coerceAtLeast(0)
            if (strLen > 0) {
                val plucked = synthesizePluckedString(freq, strLen, seedOffset = index + 1)
                for (i in 0 until strLen) {
                    mix[offsetSamples + i] += plucked[i]
                }
            }
        }

        val scale = 1.0 / sqrt(frequencies.size.toDouble())
        var peak = 0.0
        for (i in 0 until numSamples) {
            val v = abs(mix[i] * scale)
            if (v > peak) peak = v
        }
        val norm = if (peak > 1e-6) minOf(scale, 0.92 / peak) else scale
        for (i in 0 until numSamples) {
            val shaped = tanh(mix[i] * norm * 1.1)
            out[i] = (shaped * 26500.0).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return out
    }

    internal fun generateIntervalPcm(
        firstFreq: Double,
        secondFreq: Double,
        durationMs: Int = INTERVAL_DURATION_MS
    ): ShortArray {
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val out = ShortArray(numSamples)
        if (numSamples <= 0) return out

        val mix = DoubleArray(numSamples)
        // 1. Pluck root note at 0.0s
        val rootPluck = synthesizePluckedString(firstFreq, numSamples, seedOffset = 1)
        for (i in 0 until numSamples) mix[i] += rootPluck[i]

        // 2. Pluck interval note at 0.48s
        val secondOffset = (0.48 * SAMPLE_RATE).toInt()
        if (secondOffset < numSamples) {
            val rem = numSamples - secondOffset
            val secondPluck = synthesizePluckedString(secondFreq, rem, seedOffset = 2)
            for (i in 0 until rem) mix[secondOffset + i] += secondPluck[i]
        }

        // 3. Soft harmonic dyad together at 1.05s
        val dyadOffset = (1.05 * SAMPLE_RATE).toInt()
        if (dyadOffset < numSamples) {
            val rem = numSamples - dyadOffset
            val d1 = synthesizePluckedString(firstFreq, rem, seedOffset = 3)
            val d2 = synthesizePluckedString(secondFreq, rem, seedOffset = 4)
            for (i in 0 until rem) mix[dyadOffset + i] += (d1[i] + d2[i]) * 0.68
        }

        var peak = 0.0
        for (i in 0 until numSamples) {
            val v = abs(mix[i])
            if (v > peak) peak = v
        }
        val norm = if (peak > 1e-6) 0.90 / peak else 1.0
        for (i in 0 until numSamples) {
            val shaped = tanh(mix[i] * norm * 1.1)
            out[i] = (shaped * 26500.0).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return out
    }

    private fun playPcm(pcm: ShortArray) {
        if (pcm.isEmpty()) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(attrs)
            .setAudioFormat(format)
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        val previous = activeTrackRef.getAndSet(audioTrack)
        previous?.let { prev ->
            runCatching {
                if (prev.playState == AudioTrack.PLAYSTATE_PLAYING) prev.stop()
                prev.release()
            }
        }

        try {
            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()
            val sleepMs = (pcm.size * 1000L) / SAMPLE_RATE + 100L
            Thread.sleep(sleepMs)
        } catch (_: Exception) {
        } finally {
            activeTrackRef.compareAndSet(audioTrack, null)
            runCatching {
                if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) audioTrack.stop()
                audioTrack.release()
            }
        }
    }

    // 5th octave
    private const val C5 = 523.25

    // 4th octave
    private const val C4 = 261.63
    private const val Cs4 = 277.18
    private const val D4 = 293.66
    private const val Ds4 = 311.13
    private const val E4 = 329.63
    private const val F4 = 349.23
    private const val Fs4 = 369.99
    private const val G4 = 392.00
    private const val Gs4 = 415.30
    private const val A4 = 440.00
    private const val B4 = 493.88

    // 3rd octave
    private const val C3 = 130.81
    private const val G3 = 196.00
    private const val A3 = 220.00
    private const val B3 = 246.94

    fun playSoundForQuestion(answer: String, semitoneShift: Int = 0) {
        val ratio = 2.0.pow(semitoneShift / 12.0)
        fun shift(freqs: List<Double>) = freqs.map { it * ratio }

        Thread {
            when (answer) {
                // Ascending guitar voicings
                "Mayor" -> playPcm(generateStrummedPcm(shift(listOf(C3, C4, E4, G4))))
                "Menor" -> playPcm(generateStrummedPcm(shift(listOf(A3, C4, E4, A4))))
                "Aumentado" -> playPcm(generateStrummedPcm(shift(listOf(C3, E4, Gs4, C5))))
                "Disminuido" -> playPcm(generateStrummedPcm(shift(listOf(B3, D4, F4, B4))))
                "Dominante 7" -> playPcm(generateStrummedPcm(shift(listOf(G3, B3, D4, F4, G4))))
                "Mayor 7" -> playPcm(generateStrummedPcm(shift(listOf(C3, E4, G4, B4))))
                "Menor 7" -> playPcm(generateStrummedPcm(shift(listOf(A3, C4, E4, G4))))
                "Semidisminuido" -> playPcm(generateStrummedPcm(shift(listOf(B3, D4, F4, A4))))
                // Two-note melodic + harmonic intervals
                "Segunda Menor" -> playPcm(generateIntervalPcm(C4 * ratio, Cs4 * ratio))
                "Segunda Mayor" -> playPcm(generateIntervalPcm(C4 * ratio, D4 * ratio))
                "Tercera Menor" -> playPcm(generateIntervalPcm(C4 * ratio, Ds4 * ratio))
                "Tercera Mayor" -> playPcm(generateIntervalPcm(C4 * ratio, E4 * ratio))
                "Cuarta Justa" -> playPcm(generateIntervalPcm(C4 * ratio, F4 * ratio))
                "Tritono" -> playPcm(generateIntervalPcm(C4 * ratio, Fs4 * ratio))
                "Quinta Justa" -> playPcm(generateIntervalPcm(C4 * ratio, G4 * ratio))
                "Octava" -> playPcm(generateIntervalPcm(C3 * ratio, C4 * ratio))
                else -> playPcm(generateStrummedPcm(shift(listOf(C3, C4, E4, G4))))
            }
        }.start()
    }
}
