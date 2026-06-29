package com.riffforge.feature_theory.presentation.scales

import com.riffforge.feature_theory.domain.model.Scale

data class ScalesState(
    val selectedRoot: String = "E",
    val selectedScale: Scale? = null,
    val targetNotesIndices: List<Int> = emptyList()
)