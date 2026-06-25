package com.riffforge.feature_auth.domain.use_case

import com.riffforge.feature_auth.domain.model.AuthUser
import com.riffforge.feature_auth.domain.repository.AuthRepository

class SignInUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("El correo y la contraseña no pueden estar vacíos."))
        }
        return repository.signIn(email, password)
    }
}