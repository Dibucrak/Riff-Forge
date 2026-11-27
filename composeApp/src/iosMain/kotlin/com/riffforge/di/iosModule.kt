package com.riffforge.di

import com.riffforge.core.domain.sync.CloudSyncManager
import com.riffforge.feature_auth.IOSAuthManager
import com.riffforge.feature_auth.domain.AuthManager
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS Koin module — overrides common stubs with real iOS implementations.
 */
val iosModule: Module = module {
    single { IOSAuthManager() }
    single<AuthManager> { get<IOSAuthManager>() }
    single<CloudSyncManager> { get<IOSAuthManager>() }
}

/**
 * All iOS modules combined.
 */
val iosModules = listOf(
    commonModule,
    iosModule
)

