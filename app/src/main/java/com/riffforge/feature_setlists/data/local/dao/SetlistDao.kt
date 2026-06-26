package com.riffforge.feature_setlists.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.riffforge.feature_setlists.data.local.entity.SetlistEntity
import com.riffforge.feature_setlists.data.local.entity.SetlistSongCrossRef
import com.riffforge.feature_setlists.data.local.relation.SetlistWithSongsRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlist(setlist: SetlistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSetlistSongCrossRef(crossRef: SetlistSongCrossRef)

    @Transaction
    @Query("SELECT * FROM setlists ORDER BY name ASC")
    fun getSetlistsWithSongs(): Flow<List<SetlistWithSongsRelation>>

    @Transaction
    @Query("SELECT * FROM setlists WHERE setId = :setId")
    fun getSetlistById(setId: Int): Flow<SetlistWithSongsRelation?>

    @Query("DELETE FROM setlist_song_cross_ref WHERE setId = :setId AND songId = :songId")
    suspend fun removeSongFromSetlist(setId: Int, songId: Int)
}