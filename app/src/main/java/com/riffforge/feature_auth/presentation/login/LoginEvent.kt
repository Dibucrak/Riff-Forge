package com.riffforge.feature_auth.presentation.login

sealed class LoginEvent {
    data class EnteredEmail(val value: String) : LoginEvent()
    data class EnteredPassword(val value: String) : LoginEvent()
    object SignIn : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
}