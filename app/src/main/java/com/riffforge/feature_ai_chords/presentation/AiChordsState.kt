package com.riffforge.feature_ai_chords.presentation

import com.riffforge.feature_ai_chords.domain.model.AiChordsResult

data class AiChordsState(
    val input: String = "",
    val styleHint: String = "Original",
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: AiChordsResult? = null,
    val isSaved: Boolean = false,
    val savedSongId: Int? = null
)
