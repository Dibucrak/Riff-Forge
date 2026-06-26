package com.riffforge.feature_songs.domain.use_case

data class SongUseCases(
    val getSongs: GetSongsUseCase,
    val getSongById: GetSongByIdUseCase,
    val addSong: AddSongUseCase
)