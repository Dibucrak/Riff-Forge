package com.riffforge

import com.riffforge.feature_ear_training.domain.util.AudioSynth
import com.riffforge.feature_metronome.presentation.MetronomeSound
import com.riffforge.feature_metronome.presentation.MetronomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class AudioSynthTest {

    @Test
    fun metronomeClicks_allPresets_nonSilentWithSmoothFadeOut() {
        for (sound in MetronomeSound.entries) {
            for (isAccent in listOf(true, false)) {
                val pcm = MetronomeViewModel.generateClickPcm(isAccent = isAccent, sound = sound)
                assertTrue("$sound accent=$isAccent should be non-empty", pcm.isNotEmpty())
                val peak = pcm.maxOf { abs(it.toInt()) }
                assertTrue("$sound peak should be audible (> 5000), got $peak", peak > 5000)
                // Smooth attack & fade-out prevent DC pops
                assertEquals("$sound should fade to zero at final sample", 0, pcm.last().toInt())
                assertTrue(
                    "$sound first sample should ramp up smoothly",
                    abs(pcm.first().toInt()) < 500
                )
            }
        }
    }

    @Test
    fun karplusStrongChord_generatesRichDecayingSignal() {
        val pcm = AudioSynth.generateStrummedPcm(
            frequencies = listOf(130.81, 261.63, 329.63, 392.00),
            durationMs = 1650
        )
        assertTrue(pcm.isNotEmpty())
        val peak = pcm.maxOf { abs(it.toInt()) }
        assertTrue("Chord peak should be audible (> 4000), got $peak", peak > 4000)
        assertEquals("Chord should end cleanly at zero", 0, pcm.last().toInt())
    }

    @Test
    fun karplusStrongInterval_containsDistinctMelodicAndHarmonicSegments() {
        val pcm = AudioSynth.generateIntervalPcm(firstFreq = 261.63, secondFreq = 392.00)
        assertTrue(pcm.isNotEmpty())
        val peak = pcm.maxOf { abs(it.toInt()) }
        assertTrue("Interval peak should be audible (> 4000), got $peak", peak > 4000)
        val lateEnergy = pcm.drop((0.50 * AudioSynth.SAMPLE_RATE).toInt()).maxOf { abs(it.toInt()) }
        assertTrue("Second note segment should have strong signal (> 3000), got $lateEnergy", lateEnergy > 3000)
        assertEquals("Interval should end cleanly at zero", 0, pcm.last().toInt())
    }
}
