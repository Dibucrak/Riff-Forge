package com.riffforge.shared.domain.use_case

import com.riffforge.shared.domain.repository.AuthRepository

class SignOutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}
