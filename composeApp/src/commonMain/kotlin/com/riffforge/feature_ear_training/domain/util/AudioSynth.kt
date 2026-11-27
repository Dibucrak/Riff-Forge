package com.riffforge.feature_ear_training.domain.util

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Multiplatform AudioSynth — Karplus-Strong physical plucked acoustic guitar
 * string synthesizer with pick-position comb filtering, wooden body warmth,
 * realistic strumming, and melodic+harmonic interval playback.
 */
object AudioSynth {

    const val SAMPLE_RATE = 44100
    private const val CHORD_DURATION_MS = 1650
    private const val INTERVAL_DURATION_MS = 1900

    // Note frequencies (4th & 5th octave)
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
    private const val C5 = 523.25

    // Lower octave roots (3rd octave)
    private const val C3 = 130.81
    private const val Cs3 = 138.59
    private const val D3 = 146.83
    private const val Ds3 = 155.56
    private const val E3 = 164.81
    private const val F3 = 174.61
    private const val Fs3 = 185.00
    private const val G3 = 196.00
    private const val Gs3 = 207.65
    private const val A3 = 220.00
    private const val As3 = 233.08
    private const val B3 = 246.94

    fun synthesizePluckedString(
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

        val rawBurst = DoubleArray(delayLen)
        var burstLp = 0.0
        for (i in 0 until delayLen) {
            burstLp = burstLp * 0.55 + nextSignedNoise() * 0.45
            val fundamentalSeed = 0.18 * sin(2.0 * PI * i.toDouble() / period)
            rawBurst[i] = burstLp + fundamentalSeed
        }

        val pickDelay = (delayLen * 0.22).toInt().coerceAtLeast(1)
        for (i in 0 until delayLen) {
            val delayed = if (i >= pickDelay) rawBurst[i - pickDelay] else 0.0
            ring[i] = rawBurst[i] - 0.68 * delayed
        }
        ring[delayLen] = ring[0]
        ring[delayLen + 1] = ring[1]

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

            val ks = 0.5 * (interpolated + prevFiltered) * damp
            prevFiltered = interpolated
            ring[writeIdx] = ks
            writeIdx = (writeIdx + 1) % bufSize

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

    fun generateStrummedPcm(
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

    fun generateIntervalPcm(
        firstFreq: Double,
        secondFreq: Double,
        durationMs: Int = INTERVAL_DURATION_MS
    ): ShortArray {
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val out = ShortArray(numSamples)
        if (numSamples <= 0) return out

        val mix = DoubleArray(numSamples)
        val rootPluck = synthesizePluckedString(firstFreq, numSamples, seedOffset = 1)
        for (i in 0 until numSamples) mix[i] += rootPluck[i]

        val secondOffset = (0.48 * SAMPLE_RATE).toInt()
        if (secondOffset < numSamples) {
            val rem = numSamples - secondOffset
            val secondPluck = synthesizePluckedString(secondFreq, rem, seedOffset = 2)
            for (i in 0 until rem) mix[secondOffset + i] += secondPluck[i]
        }

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

    fun playSingleFrequency(frequencyHz: Float, durationMs: Int = 900) {
        val pcm = generateStrummedPcm(
            frequencies = listOf(frequencyHz.toDouble()),
            durationMs = durationMs,
            strumDelaySeconds = 0.0
        )
        playPcmSamples(pcm, SAMPLE_RATE)
    }

    fun playSoundForQuestion(answer: String) {
        when (answer) {
            "Mayor" -> playPcmSamples(generateStrummedPcm(listOf(C3, C4, E4, G4)), SAMPLE_RATE)
            "Menor" -> playPcmSamples(generateStrummedPcm(listOf(A3, C4, E4, A4)), SAMPLE_RATE)
            "Aumentado" -> playPcmSamples(generateStrummedPcm(listOf(C3, E4, Gs4, C5)), SAMPLE_RATE)
            "Disminuido" -> playPcmSamples(generateStrummedPcm(listOf(B3, D4, F4, B4)), SAMPLE_RATE)
            "Dominante 7" -> playPcmSamples(generateStrummedPcm(listOf(G3, B3, D4, F4, G4)), SAMPLE_RATE)
            "Mayor 7" -> playPcmSamples(generateStrummedPcm(listOf(C3, E4, G4, B4)), SAMPLE_RATE)
            "Menor 7" -> playPcmSamples(generateStrummedPcm(listOf(A3, C4, E4, G4)), SAMPLE_RATE)
            "Semidisminuido" -> playPcmSamples(generateStrummedPcm(listOf(B3, D4, F4, A4)), SAMPLE_RATE)
            "Segunda Menor" -> playPcmSamples(generateIntervalPcm(C4, Cs4), SAMPLE_RATE)
            "Segunda Mayor" -> playPcmSamples(generateIntervalPcm(C4, D4), SAMPLE_RATE)
            "Tercera Menor" -> playPcmSamples(generateIntervalPcm(C4, Ds4), SAMPLE_RATE)
            "Tercera Mayor" -> playPcmSamples(generateIntervalPcm(C4, E4), SAMPLE_RATE)
            "Cuarta Justa" -> playPcmSamples(generateIntervalPcm(C4, F4), SAMPLE_RATE)
            "Tritono" -> playPcmSamples(generateIntervalPcm(C4, Fs4), SAMPLE_RATE)
            "Quinta Justa" -> playPcmSamples(generateIntervalPcm(C4, G4), SAMPLE_RATE)
            "Octava" -> playPcmSamples(generateIntervalPcm(C3, C4), SAMPLE_RATE)
            else -> playPcmSamples(generateStrummedPcm(listOf(C3, C4, E4, G4)), SAMPLE_RATE)
        }
    }

    fun playChordByName(chordName: String) {
        val trimmed = chordName.trim()
        val rootRegex = Regex("""^([A-G](?:#|b)?)(.*)$""")
        val match = rootRegex.find(trimmed)
        val root = match?.groupValues?.get(1) ?: "C"
        val suffix = match?.groupValues?.get(2) ?: ""

        val rootFreq3 = when (root) {
            "C" -> C3
            "C#", "Db" -> Cs3
            "D" -> D3
            "D#", "Eb" -> Ds3
            "E" -> E3
            "F" -> F3
            "F#", "Gb" -> Fs3
            "G" -> G3
            "G#", "Ab" -> Gs3
            "A" -> A3
            "A#", "Bb" -> As3
            "B" -> B3
            else -> C3
        }
        val rootFreq4 = rootFreq3 * 2.0
        fun semitone(f: Double, st: Int): Double = f * 2.0.pow(st / 12.0)

        val intervals = when {
            suffix.contains("maj7", ignoreCase = true) -> listOf(0, 4, 7, 11)
            suffix.contains("m7b5", ignoreCase = true) || suffix.contains("dim", ignoreCase = true) -> listOf(0, 3, 6, 10)
            suffix.contains("m7", ignoreCase = true) -> listOf(0, 3, 7, 10)
            suffix.startsWith("m") && !suffix.startsWith("maj") -> listOf(0, 3, 7)
            suffix.contains("7") -> listOf(0, 4, 7, 10)
            suffix.contains("sus4") -> listOf(0, 5, 7)
            suffix.contains("sus2") -> listOf(0, 2, 7)
            else -> listOf(0, 4, 7)
        }

        val freqs = listOf(rootFreq3) + intervals.map { semitone(rootFreq4, it) }
        val pcm = generateStrummedPcm(freqs, durationMs = 1100, strumDelaySeconds = 0.022)
        playPcmSamples(pcm, SAMPLE_RATE)
    }
}

expect fun playPcmSamples(samples: ShortArray, sampleRate: Int)
