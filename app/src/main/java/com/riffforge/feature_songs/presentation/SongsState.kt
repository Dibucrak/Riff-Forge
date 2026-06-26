package com.riffforge.feature_songs.presentation

import com.riffforge.feature_songs.domain.model.Song

data class SongsState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)