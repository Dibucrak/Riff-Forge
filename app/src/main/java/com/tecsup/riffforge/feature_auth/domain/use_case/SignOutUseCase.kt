package com.riffforge.feature_auth.domain.use_case

import com.riffforge.feature_auth.domain.repository.AuthRepository

class SignOutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}