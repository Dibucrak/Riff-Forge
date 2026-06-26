package com.riffforge.feature_tuner.presentation

data class TunerState(
    val isListening: Boolean = false,
    val currentNote: String = "-",
    val currentFrequency: Float = 0f,
    val centsOffset: Float = 0f,
    val hasPermission: Boolean = false
)