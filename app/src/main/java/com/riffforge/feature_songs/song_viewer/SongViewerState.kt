package com.riffforge.feature_songs.presentation.song_viewer

import com.riffforge.feature_songs.domain.model.Song

data class SongViewerState(
    val song: Song? = null,
    val originalContent: String = "",
    val displayedContent: String = "",
    val transposeSemitones: Int = 0,
    val capo: Int = 0,
    val preferFlats: Boolean = false,
    val isAutoScrolling: Boolean = false,
    val scrollSpeed: Int = 2,
    val textSizeSp: Float = 16f,
    val isLoading: Boolean = true
)