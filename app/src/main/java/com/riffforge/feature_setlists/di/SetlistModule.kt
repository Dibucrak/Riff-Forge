package com.riffforge.feature_setlists.di

import com.riffforge.core.data.local.AppDatabase
import com.riffforge.feature_setlists.data.local.dao.SetlistDao
import com.riffforge.feature_setlists.data.repository.SetlistRepositoryImpl
import com.riffforge.feature_setlists.domain.repository.SetlistRepository
import com.riffforge.feature_setlists.domain.use_case.AddSetlistUseCase
import com.riffforge.feature_setlists.domain.use_case.AddSongToSetlistUseCase
import com.riffforge.feature_setlists.domain.use_case.GetSetlistsUseCase
import com.riffforge.feature_setlists.domain.use_case.SetlistUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SetlistModule {

    @Provides
    @Singleton
    fun provideSetlistDao(db: AppDatabase): SetlistDao {
        return db.setlistDao
    }

    @Provides
    @Singleton
    fun provideSetlistRepository(dao: SetlistDao): SetlistRepository {
        return SetlistRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSetlistUseCases(repository: SetlistRepository): SetlistUseCases {
        return SetlistUseCases(
            getSetlists = GetSetlistsUseCase(repository),
            addSetlist = AddSetlistUseCase(repository),
            addSongToSetlist = AddSongToSetlistUseCase(repository)
        )
    }
}