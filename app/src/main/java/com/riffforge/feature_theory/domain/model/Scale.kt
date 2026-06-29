package com.riffforge.feature_theory.domain.model

data class Scale(
    val name: String,
    val intervals: List<Int>,
    val description: String
)