package com.riffforge.feature_ai_chords.domain.repository

import com.riffforge.feature_ai_chords.domain.model.AiChordsResult

interface AiChordsRepository {
    suspend fun extractChords(
        input: String,
        styleHint: String = "Original"
    ): Result<AiChordsResult>
}
