package com.riffforge.core.audio

import com.riffforge.feature_ear_training.domain.util.AudioSynth as InnerAudioSynth
import com.riffforge.feature_ear_training.domain.util.playPcmSamples
import com.riffforge.feature_metronome.presentation.MetronomeSound
import com.riffforge.feature_metronome.presentation.playMetronomeClick
import kotlin.math.pow

object AudioSynth {
    private val OPEN_STRING_FREQS = listOf(82.41, 110.00, 146.83, 196.00, 246.94, 329.63)

    fun playTone(frequencyHz: Double, durationMs: Int = 900) {
        InnerAudioSynth.playSingleFrequency(frequencyHz.toFloat(), durationMs)
    }

    fun playSingleFrequency(frequencyHz: Double, durationMs: Int = 900) {
        InnerAudioSynth.playSingleFrequency(frequencyHz.toFloat(), durationMs)
    }

    fun playMidiNote(midiNote: Int, durationMs: Int = 300) {
        // MIDI 69 is A4 (440 Hz)
        val freq = 440.0 * 2.0.pow((midiNote - 69) / 12.0)
        InnerAudioSynth.playSingleFrequency(freq.toFloat(), durationMs)
    }

    fun playChordMidi(midiNotes: List<Int>) {
        val frequencies = midiNotes.map { note ->
            440.0 * 2.0.pow((note - 69) / 12.0)
        }
        val pcm = InnerAudioSynth.generateStrummedPcm(
            frequencies = frequencies,
            durationMs = 1550,
            strumDelaySeconds = 0.024
        )
        playPcmSamples(pcm, InnerAudioSynth.SAMPLE_RATE)
    }

    fun playChord(frets: List<Int>, baseFret: Int = 1) {
        val frequencies = frets.mapIndexedNotNull { stringIdx, fret ->
            if (fret < 0 || stringIdx >= OPEN_STRING_FREQS.size) {
                null
            } else {
                val actualFret = if (fret == 0) 0 else (baseFret - 1) + fret
                val openFreq = OPEN_STRING_FREQS[stringIdx]
                openFreq * 2.0.pow(actualFret / 12.0)
            }
        }
        val pcm = InnerAudioSynth.generateStrummedPcm(
            frequencies = frequencies,
            durationMs = 1250,
            strumDelaySeconds = 0.022
        )
        playPcmSamples(pcm, InnerAudioSynth.SAMPLE_RATE)
    }

    fun playInterval(rootMidi: Int, semitones: Int) {
        val rootFreq = 440.0 * 2.0.pow((rootMidi - 69) / 12.0)
        val secondFreq = rootFreq * 2.0.pow(semitones / 12.0)
        val pcm = InnerAudioSynth.generateIntervalPcm(
            firstFreq = rootFreq,
            secondFreq = secondFreq
        )
        playPcmSamples(pcm, InnerAudioSynth.SAMPLE_RATE)
    }

    fun playClick(isDownbeat: Boolean, sound: MetronomeSound = MetronomeSound.WOOD) {
        playMetronomeClick(isDownbeat, if (isDownbeat) 1 else 2, sound)
    }
}
