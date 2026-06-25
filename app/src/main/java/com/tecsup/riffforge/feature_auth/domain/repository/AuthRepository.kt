package com.riffforge.feature_auth.domain.repository

import com.riffforge.feature_auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>

    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun signUp(email: String, password: String, displayName: String): Result<AuthUser>
    suspend fun signOut()
}