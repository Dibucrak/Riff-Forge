package com.riffforge.shared.domain.model

data class Progression(
    val id: String,
    val genre: String,
    val name: String,
    val romanNumerals: String,
    val chords: List<String>,
    val description: String
)
