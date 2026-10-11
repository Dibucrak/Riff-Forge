package com.riffforge.shared.domain.repository

import com.riffforge.shared.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getSongs(): Flow<List<Song>>
    suspend fun getSongById(id: Int): Song?
    suspend fun insertSong(song: Song)
}
