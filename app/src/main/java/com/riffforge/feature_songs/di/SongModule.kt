package com.riffforge.feature_songs.di

import com.riffforge.feature_songs.data.local.dao.SongDao
import com.riffforge.feature_songs.data.repository.SongRepositoryImpl
import com.riffforge.feature_songs.domain.repository.SongRepository
import com.riffforge.feature_songs.domain.use_case.AddSongUseCase
import com.riffforge.feature_songs.domain.use_case.DeleteSongUseCase
import com.riffforge.feature_songs.domain.use_case.GetSongByIdUseCase
import com.riffforge.feature_songs.domain.use_case.GetSongsUseCase
import com.riffforge.feature_songs.domain.use_case.ObserveSongByIdUseCase
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SongModule {

    @Provides
    @Singleton
    fun provideSongRepository(dao: SongDao): SongRepository {
        return SongRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSongUseCases(repository: SongRepository): SongUseCases {
        return SongUseCases(
            getSongs = GetSongsUseCase(repository),
            getSongById = GetSongByIdUseCase(repository),
            observeSongById = ObserveSongByIdUseCase(repository),
            addSong = AddSongUseCase(repository),
            deleteSong = DeleteSongUseCase(repository)
        )
    }
}