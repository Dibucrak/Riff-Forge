package com.riffforge.feature_metronome.presentation

enum class MetronomeSound(val label: String) {
    WOOD("Madera"),
    MECHANICAL("Mecánico"),
    SOFT("Suave")
}

data class MetronomeState(
    val bpm: Int = 120,
    val isPlaying: Boolean = false,
    val beatsPerMeasure: Int = 4,
    val currentBeat: Int = 1,
    val sound: MetronomeSound = MetronomeSound.WOOD
)
