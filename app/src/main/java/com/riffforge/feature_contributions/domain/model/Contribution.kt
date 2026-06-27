package com.riffforge.feature_contributions.domain.model

data class Contribution(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val key: String = "",
    val tuning: String = "",
    val bpm: Int = 120,
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val isApproved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)