package com.riffforge.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.riffforge.feature_songs.data.local.dao.SongDao
import com.riffforge.feature_songs.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class

    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val songDao: SongDao
}