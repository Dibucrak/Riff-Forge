package com.riffforge.feature_chords.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.riffforge.feature_chords.domain.util.ChordLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChordDictionaryViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(ChordDictionaryState())
    val state: State<ChordDictionaryState> = _state

    init {
        updateChord()
    }

    fun onEvent(event: ChordDictionaryEvent) {
        when (event) {
            is ChordDictionaryEvent.SelectRoot -> {
                _state.value = state.value.copy(selectedRoot = event.root)
                updateChord()
            }
            is ChordDictionaryEvent.SelectType -> {
                _state.value = state.value.copy(selectedType = event.type)
                updateChord()
            }
        }
    }

    private fun updateChord() {
        val foundChord = ChordLibrary.chords.find {
            it.root == state.value.selectedRoot && it.type == state.value.selectedType
        }
        _state.value = state.value.copy(currentChord = foundChord)
    }
}