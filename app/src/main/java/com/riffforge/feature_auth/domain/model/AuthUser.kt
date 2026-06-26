package com.riffforge.feature_auth.domain.model

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String?
)