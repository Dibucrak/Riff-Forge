package com.riffforge.feature_songs.domain.use_case

import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow

class ObserveSongByIdUseCase(private val repository: SongRepository) {
    operator fun invoke(id: Int): Flow<Song?> = repository.observeSongById(id)
}

class DeleteSongUseCase(private val repository: SongRepository) {
    suspend operator fun invoke(songId: Int) = repository.deleteSong(songId)
}

data class SongUseCases(
    val getSongs: GetSongsUseCase,
    val getSongById: GetSongByIdUseCase,
    val observeSongById: ObserveSongByIdUseCase,
    val addSong: AddSongUseCase,
    val deleteSong: DeleteSongUseCase
)