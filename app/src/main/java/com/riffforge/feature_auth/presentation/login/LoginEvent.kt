package com.riffforge.feature_auth.presentation.login

import android.app.Activity

sealed class LoginEvent {
    data class EnteredEmail(val value: String) : LoginEvent()
    data class EnteredPassword(val value: String) : LoginEvent()
    object SignIn : LoginEvent()
    data class SignInWithGoogle(val activity: Activity) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
}
