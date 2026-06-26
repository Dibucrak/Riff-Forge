package com.riffforge.feature_tuner.domain.model

data class PitchResult(
    val frequency: Float,
    val noteName: String,
    val cents: Float
)