package com.riffforge.feature_metronome.presentation

data class MetronomeState(
    val bpm: Int = 120,
    val isPlaying: Boolean = false,
    val beatsPerMeasure: Int = 4,
    val currentBeat: Int = 1
)