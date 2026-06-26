package com.riffforge.feature_tuner.di

import com.riffforge.feature_tuner.data.analyzer.PitchAnalyzerImpl
import com.riffforge.feature_tuner.domain.analyzer.PitchAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TunerModule {

    @Provides
    @Singleton
    fun providePitchAnalyzer(): PitchAnalyzer {
        return PitchAnalyzerImpl()
    }
}