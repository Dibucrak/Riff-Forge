package com.riffforge.shared.domain.use_case

data class AuthUseCases(
    val signIn: SignInUseCase,
    val signUp: SignUpUseCase,
    val signOut: SignOutUseCase,
    val getCurrentUser: GetCurrentUserUseCase
)
