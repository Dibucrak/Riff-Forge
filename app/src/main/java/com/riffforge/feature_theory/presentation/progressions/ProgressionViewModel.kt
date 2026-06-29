package com.riffforge.feature_theory.presentation.progressions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.riffforge.feature_theory.domain.util.ProgressionLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProgressionViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(ProgressionState())
    val state: State<ProgressionState> = _state

    init {
        updateProgressions()
    }

    fun onEvent(event: ProgressionEvent) {
        when (event) {
            is ProgressionEvent.SelectGenre -> {
                _state.value = state.value.copy(selectedGenre = event.genre)
                updateProgressions()
            }
        }
    }

    private fun updateProgressions() {
        val filteredList = ProgressionLibrary.progressions.filter {
            it.genre == state.value.selectedGenre
        }
        _state.value = state.value.copy(displayedProgressions = filteredList)
    }
}