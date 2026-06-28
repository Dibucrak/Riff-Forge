package com.riffforge.feature_daily_learning.domain.model

import com.riffforge.feature_chords.domain.model.Chord

data class ChordOfTheDayInfo(
    val chord: Chord,
    val formula: String,
    val notesIncluded: String,
    val compatibleScales: List<String>,
    val theoreticalExplanation: String
)