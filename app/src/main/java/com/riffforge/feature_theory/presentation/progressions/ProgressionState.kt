package com.riffforge.feature_theory.presentation.progressions

import com.riffforge.feature_theory.domain.model.Progression

data class ProgressionState(
    val selectedGenre: String = "Rock",
    val displayedProgressions: List<Progression> = emptyList()
)