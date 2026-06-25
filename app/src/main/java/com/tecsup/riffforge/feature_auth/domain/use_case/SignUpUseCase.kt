package com.riffforge.feature_auth.domain.use_case

import com.riffforge.feature_auth.domain.model.AuthUser
import com.riffforge.feature_auth.domain.repository.AuthRepository

class SignUpUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, displayName: String): Result<AuthUser> {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Todos los campos son obligatorios."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 6 caracteres."))
        }
        return repository.signUp(email, password, displayName)
    }
}