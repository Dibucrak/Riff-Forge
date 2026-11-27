package com.riffforge.feature_auth.domain.use_case

import com.riffforge.feature_auth.domain.repository.AuthRepository

class SendPasswordResetUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(Exception("Por favor introduce un correo electrónico válido"))
        }
        return repository.sendPasswordResetEmail(email.trim())
    }
}
