package com.riffforge.feature_chords.presentation

import com.riffforge.feature_chords.domain.model.Chord

data class ChordDictionaryState(
    val selectedRoot: String = "C",
    val selectedType: String = "Mayor",
    val currentChord: Chord? = null
)