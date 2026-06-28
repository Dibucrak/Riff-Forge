package com.riffforge.feature_ear_training.presentation

import com.riffforge.feature_ear_training.domain.model.EarTrainingQuestion

data class EarTrainingState(
    val currentQuestion: EarTrainingQuestion? = null,
    val score: Int = 0,
    val totalAnswered: Int = 0,
    val isPlaying: Boolean = false,
    val hasAnsweredCurrent: Boolean = false,
    val wasAnswerCorrect: Boolean = false,
    val selectedAnswer: String = ""
)