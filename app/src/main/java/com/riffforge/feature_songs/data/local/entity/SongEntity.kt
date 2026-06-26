package com.riffforge.feature_songs.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val artist: String,
    val key: String,
    val tuning: String,
    val bpm: Int,
    val content: String = "",
    val isDraft: Boolean = false
)