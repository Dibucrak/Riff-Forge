package com.riffforge.feature_songs.domain.repository

import com.riffforge.feature_songs.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getSongs(): Flow<List<Song>>
    fun observeSongById(id: Int): Flow<Song?>
    suspend fun getSongById(id: Int): Song?
    suspend fun insertSong(song: Song)
    suspend fun deleteSong(songId: Int)
    suspend fun clearAllSongs()
}