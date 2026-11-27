package com.riffforge.feature_profile.presentation

data class ProfileState(
    val email: String = "",
    val displayName: String = "",
    val username: String = "",
    val uid: String = "",
    val isAdmin: Boolean = false,
    val linkedProviders: List<String> = emptyList(),
    val photoUrl: String = "",
    val photoData: String = "",
    val isUpdatingPhoto: Boolean = false,
    val isLinking: Boolean = false,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val isDeletingAccount: Boolean = false
)
