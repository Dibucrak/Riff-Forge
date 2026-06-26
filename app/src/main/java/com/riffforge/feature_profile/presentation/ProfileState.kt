package com.riffforge.feature_profile.presentation

data class ProfileState(
    val email: String = "",
    val displayName: String = "",
    val uid: String = "",
    val isLoading: Boolean = true
)