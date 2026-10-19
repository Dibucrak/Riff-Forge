package com.riffforge.feature_metronome.presentation

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
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
import kotlin.math.exp
import kotlin.math.sin

@HiltViewModel
class MetronomeViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(MetronomeState())
    val state: State<MetronomeState> = _state

    private var tickerJob: Job? = null

    private val SAMPLE_RATE = 44100

    /**
     * Generate a realistic metronome click sound using a short noise burst
     * with a high-frequency tone, similar to iOS metronome apps.
     */
    private fun generateClick(isAccent: Boolean): ShortArray {
        val durationMs = if (isAccent) 40 else 25
        val numSamples = (durationMs * SAMPLE_RATE) / 1000
        val click = ShortArray(numSamples)

        val freq = if (isAccent) 1000.0 else 600.0
        val volume = if (isAccent) 0.6 else 0.35

        for (i in 0 until numSamples) {
            val time = i.toDouble() / SAMPLE_RATE
            // Fast attack + exponential decay envelope
            val envelope = exp(-30.0 * time)
            // Mix of tone and noise for realistic click
            val tone = sin(2 * Math.PI * freq * time)
            val noise = (Math.random() * 2 - 1) * 0.15
            val sample = (tone * 0.85 + noise) * envelope * volume
            click[i] = (sample * 32767).toInt().toShort()
        }
        return click
    }

    // Pre-generate click sounds
    private val accentClick by lazy { generateClick(true) }
    private val regularClick by lazy { generateClick(false) }

    // Reusable AudioTrack instances to avoid creation/destruction overhead on every beat
    private var accentTrack: AudioTrack? = null
    private var regularTrack: AudioTrack? = null

    private fun getAudioTrack(isAccent: Boolean): AudioTrack {
        return if (isAccent) {
            accentTrack ?: run {
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, accentClick.size,
                    AudioTrack.MODE_STATIC
                )
                track.write(accentClick, 0, accentClick.size)
                accentTrack = track
                track
            }
        } else {
            regularTrack ?: run {
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, regularClick.size,
                    AudioTrack.MODE_STATIC
                )
                track.write(regularClick, 0, regularClick.size)
                regularTrack = track
                track
            }
        }
    }

    // Duration of click sounds in ms, used to compensate delay for accurate timing
    private val accentClickDurationMs = 40L
    private val regularClickDurationMs = 25L

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
                val isFirstBeat = _state.value.currentBeat == 1
                playTick(isFirstBeat)

                // Calculate exact interval between beats in ms
                val intervalMs = 60000L / _state.value.bpm
                // Compensate for the click sound duration so the next beat starts
                // exactly at the interval, not after the sound finishes playing
                val clickDuration = if (isFirstBeat) accentClickDurationMs else regularClickDurationMs
                val waitMs = intervalMs - clickDuration
                if (waitMs > 0) {
                    delay(waitMs)
                }

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
        val track = getAudioTrack(isFirstBeat)
        track.stop()
        track.play()
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
        // Release reusable AudioTrack instances
        accentTrack?.run { stop(); release() }
        regularTrack?.run { stop(); release() }
        accentTrack = null
        regularTrack = null
    }
}