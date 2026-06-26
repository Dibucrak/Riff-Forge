package com.riffforge.feature_tuner.presentation

sealed class TunerEvent {
    object StartListening : TunerEvent()
    object StopListening : TunerEvent()
    data class PermissionResult(val isGranted: Boolean) : TunerEvent()
}