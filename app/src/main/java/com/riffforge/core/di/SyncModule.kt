package com.riffforge.core.di

import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.core.data.repository.CloudSyncRepositoryImpl
import com.riffforge.core.domain.repository.CloudSyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideCloudSyncRepository(firestore: FirebaseFirestore): CloudSyncRepository {
        return CloudSyncRepositoryImpl(firestore)
    }
}