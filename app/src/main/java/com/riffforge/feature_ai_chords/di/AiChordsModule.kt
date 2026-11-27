package com.riffforge.feature_ai_chords.di

import com.riffforge.feature_ai_chords.data.repository.AiChordsRepositoryImpl
import com.riffforge.feature_ai_chords.domain.repository.AiChordsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiChordsModule {

    @Binds
    @Singleton
    abstract fun bindAiChordsRepository(
        impl: AiChordsRepositoryImpl
    ): AiChordsRepository
}
