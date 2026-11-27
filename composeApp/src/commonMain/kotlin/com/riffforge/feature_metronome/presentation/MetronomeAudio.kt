package com.riffforge.feature_metronome.presentation

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh

enum class MetronomeSound(val label: String) {
    WOOD("Madera"),
    MECHANICAL("Mecánico"),
    SOFT("Suave")
}

object MetronomeSynth {
    const val SAMPLE_RATE = 44100

    fun generateClickPcm(isAccent: Boolean, sound: MetronomeSound = MetronomeSound.WOOD): ShortArray {
        val durationMs = when (sound) {
            MetronomeSound.WOOD -> if (isAccent) 50 else 38
            MetronomeSound.MECHANICAL -> if (isAccent) 32 else 24
            MetronomeSound.SOFT -> if (isAccent) 52 else 42
        }
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val out = ShortArray(numSamples)

        val attackSec = when (sound) {
            MetronomeSound.WOOD -> 0.0004
            MetronomeSound.MECHANICAL -> 0.0003
            MetronomeSound.SOFT -> 0.0012
        }
        val releaseSec = when (sound) {
            MetronomeSound.WOOD -> 0.004
            MetronomeSound.MECHANICAL -> 0.003
            MetronomeSound.SOFT -> 0.004
        }
        val attackSamples = (attackSec * SAMPLE_RATE).toInt().coerceAtLeast(1)
        val releaseSamples = (releaseSec * SAMPLE_RATE).toInt().coerceAtLeast(1)
        val targetPeak = if (isAccent) 28000.0 else 21000.0

        // Deterministic xorshift32 PRNG for clean, reproducible transient bursts
        var rngState = if (isAccent) 0x5EED1001 else 0x5EED2002
        fun nextSignedNoise(): Double {
            var x = rngState
            x = x xor (x shl 13)
            x = x xor (x ushr 17)
            x = x xor (x shl 5)
            rngState = x
            return ((x ushr 1).toDouble() / Int.MAX_VALUE.toDouble()) * 2.0 - 1.0
        }

        var lpNoise = 0.0
        var phaseMain = 0.0
        var phaseSecondary = 0.0
        val dt = 1.0 / SAMPLE_RATE

        for (i in 0 until numSamples) {
            val t = i * dt

            // Smooth cosine attack & release ramps to prevent DC clicks
            val attackEnv = if (i < attackSamples) {
                0.5 - 0.5 * cos(PI * i / attackSamples)
            } else 1.0
            val releaseEnv = if (i >= numSamples - releaseSamples) {
                val rem = (numSamples - 1 - i).coerceAtLeast(0)
                0.5 - 0.5 * cos(PI * rem / releaseSamples)
            } else 1.0

            val bodyRaw: Double
            when (sound) {
                MetronomeSound.WOOD -> {
                    val fStart = if (isAccent) 1520.0 else 1040.0
                    val fEnd = if (isAccent) 1080.0 else 760.0
                    val glide = exp(-t / 0.0035)
                    val instFreq = fEnd + (fStart - fEnd) * glide
                    phaseMain += 2.0 * PI * instFreq * dt
                    phaseSecondary += 2.0 * PI * (fEnd * 2.42) * dt

                    val decay = exp((if (isAccent) -78.0 else -92.0) * t)
                    val inharmonic = sin(phaseSecondary) * 0.28 * exp(-210.0 * t)
                    lpNoise = lpNoise * 0.65 + nextSignedNoise() * 0.35
                    val mallet = if (t < 0.002) lpNoise * 0.30 * exp(-1200.0 * t) else 0.0
                    bodyRaw = (sin(phaseMain) * 0.85 + inharmonic) * decay + mallet
                }
                MetronomeSound.MECHANICAL -> {
                    val fStart = if (isAccent) 2400.0 else 1680.0
                    val fEnd = if (isAccent) 1650.0 else 1180.0
                    val glide = exp(-t / 0.0025)
                    val instFreq = fEnd + (fStart - fEnd) * glide
                    phaseMain += 2.0 * PI * instFreq * dt
                    val caseFreq = if (isAccent) 820.0 else 620.0
                    phaseSecondary += 2.0 * PI * caseFreq * dt

                    val decay = exp((if (isAccent) -140.0 else -160.0) * t)
                    lpNoise = lpNoise * 0.5 + nextSignedNoise() * 0.5
                    val snap = if (t < 0.0015) lpNoise * 0.42 * exp(-1600.0 * t) else 0.0
                    bodyRaw = (sin(phaseMain) * 0.72 + sin(phaseSecondary) * 0.40) * decay + snap
                }
                MetronomeSound.SOFT -> {
                    val fBase = if (isAccent) 880.0 else 587.33
                    phaseMain += 2.0 * PI * fBase * dt
                    phaseSecondary += 2.0 * PI * (fBase * 2.0) * dt

                    val decay = exp((if (isAccent) -62.0 else -74.0) * t)
                    bodyRaw = (sin(phaseMain) * 0.86 + sin(phaseSecondary) * 0.16 * exp(-120.0 * t)) * decay
                }
            }

            val shaped = tanh(bodyRaw * 1.15) * attackEnv * releaseEnv
            out[i] = (shaped * targetPeak).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return out
    }
}

expect fun playMetronomeClick(
    isAccent: Boolean,
    beat: Int,
    sound: MetronomeSound = MetronomeSound.WOOD
)
