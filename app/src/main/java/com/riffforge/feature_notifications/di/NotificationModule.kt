package com.riffforge.feature_notifications.di

import android.content.Context
import com.riffforge.feature_notifications.data.manager.ReminderManagerImpl
import com.riffforge.feature_notifications.domain.manager.ReminderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideReminderManager(@ApplicationContext context: Context): ReminderManager {
        return ReminderManagerImpl(context)
    }
}