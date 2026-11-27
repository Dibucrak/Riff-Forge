package com.riffforge.feature_ai_chords.domain.model

data class AiChordsResult(
    val title: String,
    val artist: String,
    val key: String,
    val tuning: String,
    val bpm: Int,
    val capo: Int,
    val difficulty: String,
    val strummingPattern: String,
    val chords: List<String>,
    val content: String,
    val practiceTip: String,
    val youtubeUrl: String = "",
    val youtubeVideoId: String = "",
    val thumbnailUrl: String = "",
    val videoTitle: String = "",
    val model: String = ""
)
