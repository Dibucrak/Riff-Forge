package com.riffforge.feature_contributions.di

import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.feature_contributions.data.repository.CommunityRepositoryImpl
import com.riffforge.feature_contributions.domain.repository.CommunityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommunityModule {

    @Provides
    @Singleton
    fun provideCommunityRepository(firestore: FirebaseFirestore): CommunityRepository {
        return CommunityRepositoryImpl(firestore)
    }
}