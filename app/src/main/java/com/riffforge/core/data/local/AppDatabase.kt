package com.riffforge.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.riffforge.feature_setlists.data.local.dao.SetlistDao
import com.riffforge.feature_setlists.data.local.entity.SetlistEntity
import com.riffforge.feature_setlists.data.local.entity.SetlistSongCrossRef
import com.riffforge.feature_songs.data.local.dao.SongDao
import com.riffforge.feature_songs.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        SetlistEntity::class,
        SetlistSongCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val setlistDao: SetlistDao
}