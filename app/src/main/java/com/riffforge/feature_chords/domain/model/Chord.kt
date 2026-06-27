package com.riffforge.feature_chords.domain.model

data class Chord(
    val root: String,
    val type: String,
    val frets: List<Int>,
    val fingers: List<Int>,
    val startingFret: Int = 1
)