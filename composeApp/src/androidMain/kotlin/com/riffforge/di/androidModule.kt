package com.riffforge.di

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.feature_auth.FirebaseAuthManager
import com.riffforge.feature_auth.domain.AuthManager
import com.riffforge.feature_auth.domain.NoOpAuthManager
import org.koin.core.module.Module
import org.koin.dsl.module

import com.riffforge.core.domain.sync.CloudSyncManager
import com.riffforge.core.domain.sync.NoOpCloudSyncManager

/**
 * Android Koin module for composeApp — provides real Firebase Auth and CloudSync implementations
 * if Firebase is configured, otherwise falls back to NoOp stubs.
 */
val androidModule: Module = module {
    single {
        try {
            FirebaseApp.getInstance()
            FirebaseAuthManager(
                firebaseAuth = FirebaseAuth.getInstance(),
                firestore = FirebaseFirestore.getInstance()
            )
        } catch (e: Exception) {
            null
        }
    }
    single<AuthManager> {
        getOrNull<FirebaseAuthManager>() ?: NoOpAuthManager()
    }
    single<CloudSyncManager> {
        getOrNull<FirebaseAuthManager>() ?: NoOpCloudSyncManager()
    }
}

/**
 * All Android modules combined.
 * Note: androidModule overrides commonModule's NoOpAuthManager.
 */
val androidModules = listOf(
    commonModule,
    androidModule
)
