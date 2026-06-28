package com.riffforge.feature_ear_training.domain.model

data class EarTrainingQuestion(
    val id: String,
    val type: QuestionType,
    val correctAnswer: String,
    val options: List<String>,
    val soundUri: String = ""
)

enum class QuestionType {
    INTERVAL, CHORD, PROGRESSION
}