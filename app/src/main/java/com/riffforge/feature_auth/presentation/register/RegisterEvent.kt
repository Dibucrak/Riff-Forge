package com.riffforge.feature_auth.presentation.register

sealed class RegisterEvent {
    data class EnteredDisplayName(val value: String) : RegisterEvent()
    data class EnteredEmail(val value: String) : RegisterEvent()
    data class EnteredPassword(val value: String) : RegisterEvent()
    data class EnteredRepeatedPassword(val value: String) : RegisterEvent()
    object SignUp : RegisterEvent()
    object TogglePasswordVisibility : RegisterEvent()
    object ToggleRepeatedPasswordVisibility : RegisterEvent()
}