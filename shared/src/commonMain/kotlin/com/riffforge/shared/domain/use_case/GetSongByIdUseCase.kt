package com.riffforge.shared.domain.use_case

import com.riffforge.shared.domain.model.Song
import com.riffforge.shared.domain.repository.SongRepository

class GetSongByIdUseCase(
    private val repository: SongRepository
) {
    suspend operator fun invoke(id: Int): Song? {
        return repository.getSongById(id)
    }
}
