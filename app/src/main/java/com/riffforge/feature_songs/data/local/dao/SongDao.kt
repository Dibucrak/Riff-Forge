package com.riffforge.feature_songs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.riffforge.feature_songs.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs WHERE isDraft = 0 ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Int): SongEntity?

    @Query("SELECT * FROM songs WHERE id = :id")
    fun observeSongById(id: Int): Flow<SongEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Query("DELETE FROM setlist_song_cross_ref WHERE songId = :songId")
    suspend fun deleteSongFromAllSetlists(songId: Int)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSongById(songId: Int)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
}