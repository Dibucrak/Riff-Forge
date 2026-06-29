package com.riffforge.feature_theory.presentation.scales

import com.riffforge.feature_theory.domain.model.Scale

sealed class ScalesEvent {
    data class SelectRoot(val root: String) : ScalesEvent()
    data class SelectScale(val scale: Scale) : ScalesEvent()
}