package com.riffforge.feature_auth.presentation.login

data class LoginState(
    val emailText: String = "",
    val passwordText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)