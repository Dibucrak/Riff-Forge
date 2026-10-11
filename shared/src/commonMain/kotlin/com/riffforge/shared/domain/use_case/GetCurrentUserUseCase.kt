package com.riffforge.shared.domain.use_case

import com.riffforge.shared.domain.model.AuthUser
import com.riffforge.shared.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> {
        return repository.currentUser
    }
}
