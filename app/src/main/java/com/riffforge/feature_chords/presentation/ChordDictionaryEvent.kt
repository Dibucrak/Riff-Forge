package com.riffforge.feature_chords.presentation

sealed class ChordDictionaryEvent {
    data class SelectRoot(val root: String) : ChordDictionaryEvent()
    data class SelectType(val type: String) : ChordDictionaryEvent()
}