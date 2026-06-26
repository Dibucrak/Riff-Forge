package com.riffforge.feature_auth.presentation.register

data class RegisterState(
    val displayNameText: String = "",
    val emailText: String = "",
    val passwordText: String = "",
    val repeatedPasswordText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)