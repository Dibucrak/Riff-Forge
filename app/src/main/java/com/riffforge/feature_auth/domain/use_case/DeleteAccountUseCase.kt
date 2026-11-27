package com.riffforge.feature_auth.domain.use_case

import com.riffforge.feature_auth.domain.repository.AuthRepository

class DeleteAccountUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.deleteAccount()
    }
}
