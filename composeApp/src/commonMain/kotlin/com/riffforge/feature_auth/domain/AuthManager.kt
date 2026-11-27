package com.riffforge.feature_auth.domain

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic auth manager interface.
 * Each platform (Android/iOS) provides its own implementation via Koin.
 */
interface AuthManager {

    val currentUser: Flow<AuthUser?>

    suspend fun signIn(email: String, password: String): Result<AuthUser>

    suspend fun signUp(email: String, password: String, displayName: String): Result<AuthUser>

    suspend fun signInWithGoogle(): Result<AuthUser>

    suspend fun linkWithGoogle(): Result<AuthUser>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun deleteAccount(): Result<Unit>

    suspend fun signOut()

    fun isAuthenticated(): Boolean
}
