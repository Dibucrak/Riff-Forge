package com.riffforge.feature_tuner.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_tuner.domain.analyzer.PitchAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class TunerViewModel @Inject constructor(
    private val pitchAnalyzer: PitchAnalyzer
) : ViewModel() {

    private val _state = mutableStateOf(TunerState())
    val state: State<TunerState> = _state

    private var analysisJob: Job? = null

    fun onEvent(event: TunerEvent) {
        when (event) {
            is TunerEvent.PermissionResult -> {
                _state.value = state.value.copy(hasPermission = event.isGranted)
                if (event.isGranted) {
                    startListening()
                }
            }
            is TunerEvent.StartListening -> {
                if (state.value.hasPermission) {
                    startListening()
                }
            }
            is TunerEvent.StopListening -> {
                stopListening()
            }
        }
    }

    private fun startListening() {
        if (state.value.isListening) return

        _state.value = state.value.copy(isListening = true)
        analysisJob?.cancel()

        analysisJob = pitchAnalyzer.startAnalyzing()
            .onEach { pitchResult ->
                if (pitchResult != null) {
                    _state.value = state.value.copy(
                        currentNote = pitchResult.noteName,
                        currentFrequency = pitchResult.frequency,
                        centsOffset = pitchResult.cents
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun stopListening() {
        _state.value = state.value.copy(
            isListening = false,
            currentNote = "-",
            currentFrequency = 0f,
            centsOffset = 0f
        )
        analysisJob?.cancel()
        pitchAnalyzer.stopAnalyzing()
    }

    override fun onCleared() {
        super.onCleared()
        pitchAnalyzer.stopAnalyzing()
    }
}