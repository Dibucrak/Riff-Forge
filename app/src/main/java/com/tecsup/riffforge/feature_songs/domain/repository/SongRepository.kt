package com.riffforge.feature_songs.domain.repository

import com.riffforge.feature_songs.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getSongs(): Flow<List<Song>>
    suspend fun insertSong(song: Song)
}