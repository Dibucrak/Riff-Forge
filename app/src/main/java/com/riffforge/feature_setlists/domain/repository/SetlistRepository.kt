package com.riffforge.feature_setlists.domain.repository

import com.riffforge.feature_setlists.domain.model.Setlist
import com.riffforge.feature_setlists.domain.model.SetlistDetail
import kotlinx.coroutines.flow.Flow

interface SetlistRepository {
    fun getSetlists(): Flow<List<SetlistDetail>>
    fun getSetlistById(id: Int): Flow<SetlistDetail?>
    suspend fun insertSetlist(setlist: Setlist): Long
    suspend fun addSongToSetlist(setId: Int, songId: Int)
    suspend fun removeSongFromSetlist(setId: Int, songId: Int)
}