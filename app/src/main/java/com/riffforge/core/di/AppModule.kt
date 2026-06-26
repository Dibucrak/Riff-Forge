package com.riffforge.core.di

import android.app.Application
import androidx.room.Room
import com.riffforge.core.data.local.AppDatabase
import com.riffforge.feature_songs.data.local.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "riffforge_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSongDao(db: AppDatabase): SongDao {
        return db.songDao
    }
}