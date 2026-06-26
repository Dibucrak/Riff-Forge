package com.riffforge.feature_songs.presentation

import com.riffforge.feature_songs.domain.model.Song

sealed class SongsEvent {
    object LoadSongs : SongsEvent()
    data class DeleteSong(val song: Song) : SongsEvent()
}