package com.riffforge.feature_songs.domain.use_case

import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow

class GetSongsUseCase(
    private val repository: SongRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return repository.getSongs()
    }
}