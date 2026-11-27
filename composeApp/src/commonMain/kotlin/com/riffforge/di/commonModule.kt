package com.riffforge.di

import com.riffforge.core.domain.sync.CloudSyncManager
import com.riffforge.core.domain.sync.NoOpCloudSyncManager
import com.riffforge.feature_auth.domain.AuthManager
import com.riffforge.feature_auth.domain.NoOpAuthManager
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Common Koin module — provides NoOp stubs for features that require
 * platform-specific implementations (Firebase, etc.).
 */
val commonModule: Module = module {
    single<AuthManager> { NoOpAuthManager() }
    single<CloudSyncManager> { NoOpCloudSyncManager() }
}
