package com.riffforge.feature_admin.domain.repository

import com.riffforge.feature_admin.domain.model.AdminUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository para la gestión de usuarios desde el panel de administración.
 */
interface UserRepository {
    /** Fluye la lista de todos los usuarios registrados. */
    fun getAllUsers(): Flow<List<AdminUser>>

    /** Otorga o revoca el rol de administrador a un usuario. */
    suspend fun updateAdminRole(uid: String, isAdmin: Boolean): Result<Unit>

    /** Elimina un usuario de la plataforma. */
    suspend fun deleteUser(uid: String): Result<Unit>
}
