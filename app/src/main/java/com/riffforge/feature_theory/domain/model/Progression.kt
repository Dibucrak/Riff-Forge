package com.riffforge.feature_theory.domain.model

data class Progression(
    val id: String,
    val genre: String,
    val name: String,
    val romanNumerals: String,
    val chords: List<String>,
    val description: String
)