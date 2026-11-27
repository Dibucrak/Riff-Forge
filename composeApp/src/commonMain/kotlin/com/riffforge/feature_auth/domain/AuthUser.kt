package com.riffforge.feature_auth.domain

/**
 * Shared auth user model for KMP.
 */
data class AuthUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val username: String = "",
    val isAdmin: Boolean = false,
    val linkedProviders: List<String> = emptyList(),
    val photoUrl: String = "",
    val photoData: String = ""
)
