package com.riffforge.feature_admin.domain.model

/**
 * Usuario registrado en la plataforma con información para gestión administrativa.
 */
data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String?,
    val isAdmin: Boolean = false,
    val joinDate: Long = System.currentTimeMillis(),
    val contributionCount: Int = 0
)
