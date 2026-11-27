package com.riffforge.feature_songs.presentation.song_viewer

import com.riffforge.core.domain.util.AppSettings
import com.riffforge.feature_songs.domain.model.Song

data class SongViewerState(
    val song: Song? = null,
    val originalContent: String = "",
    val displayedContent: String = "",
    val transposeSemitones: Int = 0,
    val capo: Int = 0,
    val preferFlats: Boolean = false,
    val useSpanishNotation: Boolean = AppSettings.useSpanishNotation,
    val isAutoScrolling: Boolean = false,
    val scrollSpeed: Float = 1.0f,
    val textSizeSp: Float = 16f,
    val isLoading: Boolean = true,

    val currentSetlistId: Int? = null,
    val setlistName: String? = null,
    val nextSongId: Int? = null,
    val previousSongId: Int? = null,
    val currentSongIndex: Int = 0,
    val totalSongsInSet: Int = 0
)