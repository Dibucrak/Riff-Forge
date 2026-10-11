package com.riffforge.shared.domain.use_case

data class SongUseCases(
    val getSongs: GetSongsUseCase,
    val getSongById: GetSongByIdUseCase,
    val addSong: AddSongUseCase
)
