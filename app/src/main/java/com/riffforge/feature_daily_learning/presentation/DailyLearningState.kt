package com.riffforge.feature_daily_learning.presentation

import com.riffforge.feature_daily_learning.domain.model.ChordOfTheDayInfo

data class DailyLearningState(
    val chordOfTheDay: ChordOfTheDayInfo? = null,
    val isLoading: Boolean = true
)