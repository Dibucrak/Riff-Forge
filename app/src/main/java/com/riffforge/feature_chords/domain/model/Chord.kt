package com.riffforge.feature_chords.domain.model

data class Chord(
    val id: String = "",
    val root: String,
    val type: String,
    val stringFrets: List<Int>,
    val fingers: List<Int> = listOf(0, 0, 0, 0, 0, 0),
    val difficulty: String = "Media",
    val isBarre: Boolean = false,
    val barreFret: Int = 0,
    val startingFret: Int = 1
) {
    // Compatibility property for UI that uses 'frets'
    val frets: List<Int> get() = stringFrets
}