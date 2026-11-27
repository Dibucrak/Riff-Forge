package com.riffforge.feature_ai_chords.domain

import kotlinx.serialization.Serializable

@Serializable
data class AiChordsRequest(
    val input: String,
    val styleHint: String = "Original",
    val language: String = "es"
)

@Serializable
data class AiChordsErrorResponse(
    val error: String = ""
)

@Serializable
data class AiChordsResult(
    val title: String = "Canción IA",
    val artist: String = "YouTube",
    val key: String = "C",
    val tuning: String = "E Standard",
    val bpm: Int = 120,
    val capo: Int = 0,
    val difficulty: String = "Intermedio",
    val strummingPattern: String = "D - D U - U D U",
    val chords: List<String> = emptyList(),
    val content: String = "",
    val practiceTip: String = "",
    val youtubeUrl: String = "",
    val youtubeVideoId: String = "",
    val thumbnailUrl: String = "",
    val videoTitle: String = "",
    val model: String = ""
)
