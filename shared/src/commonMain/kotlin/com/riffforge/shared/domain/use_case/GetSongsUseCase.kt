package com.riffforge.shared.domain.use_case

import com.riffforge.shared.domain.model.Song
import com.riffforge.shared.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow

class GetSongsUseCase(
    private val repository: SongRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return repository.getSongs()
    }
}
