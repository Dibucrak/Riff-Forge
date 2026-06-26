package com.riffforge.feature_profile.presentation

sealed class ProfileEvent {
    object SignOut : ProfileEvent()
}