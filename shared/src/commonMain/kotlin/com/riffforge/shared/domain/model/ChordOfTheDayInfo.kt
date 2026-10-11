package com.riffforge.shared.domain.model

data class ChordOfTheDayInfo(
    val chord: Chord,
    val formula: String,
    val notesIncluded: String,
    val compatibleScales: List<String>,
    val theoreticalExplanation: String
)
