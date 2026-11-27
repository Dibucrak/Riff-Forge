package com.riffforge.feature_auth.domain.use_case

data class AuthUseCases(
    val signIn: SignInUseCase,
    val signUp: SignUpUseCase,
    val signInWithGoogle: SignInWithGoogleUseCase,
    val linkWithGoogle: LinkWithGoogleUseCase,
    val sendPasswordReset: SendPasswordResetUseCase,
    val deleteAccount: DeleteAccountUseCase,
    val signOut: SignOutUseCase,
    val getCurrentUser: GetCurrentUserUseCase
)
