package com.riffforge.feature_songs.domain.use_case

import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.domain.repository.SongRepository

class GetSongByIdUseCase(
    private val repository: SongRepository
) {
    suspend operator fun invoke(id: Int): Song? {
        return repository.getSongById(id)
    }
}