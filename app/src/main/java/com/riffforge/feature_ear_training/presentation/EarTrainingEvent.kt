package com.riffforge.feature_ear_training.presentation

sealed class EarTrainingEvent {
    data class SubmitAnswer(val answer: String) : EarTrainingEvent()
    object PlayAudio : EarTrainingEvent()
    object NextQuestion : EarTrainingEvent()
}