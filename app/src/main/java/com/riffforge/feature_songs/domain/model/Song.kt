package com.riffforge.feature_songs.domain.model

data class Song(
    val id: Int = 0,
    val title: String,
    val artist: String,
    val key: String,
    val tuning: String,
    val bpm: Int,
    val content: String = "",
    val isDraft: Boolean = false
)