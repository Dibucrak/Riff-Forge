package com.riffforge.feature_metronome.presentation

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MetronomeViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(MetronomeState())
    val state: State<MetronomeState> = _state

    private var tickerJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    }

    fun onEvent(event: MetronomeEvent) {
        when (event) {
            is MetronomeEvent.TogglePlayPause -> {
                val isPlaying = !_state.value.isPlaying
                _state.value = state.value.copy(
                    isPlaying = isPlaying,
                    currentBeat = 1
                )
                if (isPlaying) {
                    startTicker()
                } else {
                    stopTicker()
                }
            }
            is MetronomeEvent.ChangeBpm -> {
                _state.value = state.value.copy(bpm = event.bpm)
            }
            is MetronomeEvent.ChangeBeatsPerMeasure -> {
                _state.value = state.value.copy(
                    beatsPerMeasure = event.beats,
                    currentBeat = 1
                )
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive && _state.value.isPlaying) {
                playTick(_state.value.currentBeat == 1)

                val delayMs = 60000L / _state.value.bpm
                delay(delayMs)

                val nextBeat = if (_state.value.currentBeat >= _state.value.beatsPerMeasure) {
                    1
                } else {
                    _state.value.currentBeat + 1
                }

                _state.value = state.value.copy(currentBeat = nextBeat)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
    }

    private fun playTick(isFirstBeat: Boolean) {
        if (isFirstBeat) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } else {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 30)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
        toneGenerator?.release()
        toneGenerator = null
    }
}