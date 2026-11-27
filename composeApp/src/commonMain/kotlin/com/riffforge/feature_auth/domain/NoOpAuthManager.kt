package com.riffforge.feature_auth.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory implementation of AuthManager used when Firebase is not configured.
 * Allows local sign-in, registration, and sign-out for testing and offline sessions.
 */
class NoOpAuthManager : AuthManager {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: Flow<AuthUser?> = _currentUser.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        val cleanEmail = email.trim()
        val namePart = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercaseChar() }
        val user = AuthUser(
            uid = "local_${cleanEmail.hashCode()}",
            email = cleanEmail,
            displayName = namePart,
            username = cleanEmail.substringBefore("@").lowercase()
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Result<AuthUser> {
        val cleanEmail = email.trim()
        val cleanName = displayName.trim().ifBlank { cleanEmail.substringBefore("@") }
        val user = AuthUser(
            uid = "local_${cleanEmail.hashCode()}",
            email = cleanEmail,
            displayName = cleanName,
            username = cleanName.lowercase().replace(" ", "_")
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(): Result<AuthUser> {
        return Result.failure(Exception("Firebase Auth no está inicializado en modo sin conexión"))
    }

    override suspend fun linkWithGoogle(): Result<AuthUser> {
        return Result.failure(Exception("Vinculación con Google requiere Firebase Auth"))
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(Exception("Introduce un correo electrónico válido"))
        }
        return Result.success(Unit)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }

    override fun isAuthenticated(): Boolean {
        return _currentUser.value != null
    }
}
