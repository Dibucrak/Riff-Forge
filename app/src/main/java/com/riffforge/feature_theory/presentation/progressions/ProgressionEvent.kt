package com.riffforge.feature_theory.presentation.progressions

sealed class ProgressionEvent {
    data class SelectGenre(val genre: String) : ProgressionEvent()
}