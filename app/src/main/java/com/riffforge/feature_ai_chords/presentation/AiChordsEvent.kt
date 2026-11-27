package com.riffforge.feature_ai_chords.presentation

sealed class AiChordsEvent {
    data class InputChanged(val value: String) : AiChordsEvent()
    data class StyleChanged(val styleHint: String) : AiChordsEvent()
    data class ApplySample(val sampleInput: String) : AiChordsEvent()
    data object ExtractChords : AiChordsEvent()
    data object SaveToRepertoire : AiChordsEvent()
}
