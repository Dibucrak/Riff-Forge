package com.riffforge.feature_theory.presentation.scales

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.riffforge.feature_theory.domain.util.ScaleLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScalesViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(ScalesState())
    val state: State<ScalesState> = _state

    init {
        onEvent(ScalesEvent.SelectScale(ScaleLibrary.scales[2]))
    }

    fun onEvent(event: ScalesEvent) {
        when (event) {
            is ScalesEvent.SelectRoot -> {
                _state.value = state.value.copy(selectedRoot = event.root)
                calculateTargetNotes()
            }
            is ScalesEvent.SelectScale -> {
                _state.value = state.value.copy(selectedScale = event.scale)
                calculateTargetNotes()
            }
        }
    }

    private fun calculateTargetNotes() {
        val rootStr = state.value.selectedRoot
        val scale = state.value.selectedScale ?: return

        val rootIndex = ScaleLibrary.roots.indexOf(rootStr)
        if (rootIndex == -1) return

        val absoluteNotes = scale.intervals.map { interval ->
            (rootIndex + interval) % 12
        }

        _state.value = state.value.copy(targetNotesIndices = absoluteNotes)
    }
}