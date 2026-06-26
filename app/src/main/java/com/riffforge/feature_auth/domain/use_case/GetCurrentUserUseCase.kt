package com.riffforge.feature_auth.domain.use_case

import com.riffforge.feature_auth.domain.model.AuthUser
import com.riffforge.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> {
        return repository.currentUser
    }
}