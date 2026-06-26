package com.riffforge.feature_metronome.presentation

sealed class MetronomeEvent {
    object TogglePlayPause : MetronomeEvent()
    data class ChangeBpm(val bpm: Int) : MetronomeEvent()
    data class ChangeBeatsPerMeasure(val beats: Int) : MetronomeEvent()
}