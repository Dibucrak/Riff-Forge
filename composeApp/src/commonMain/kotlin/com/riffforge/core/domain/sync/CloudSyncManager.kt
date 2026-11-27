package com.riffforge.core.domain.sync

import com.riffforge.core.domain.store.AdminUserModel
import com.riffforge.core.domain.store.CommunityPostModel
import com.riffforge.core.domain.store.SongItemModel

/**
 * Cross-platform interface for synchronizing the catalog, community contributions,
 * profile photo, and admin user roles with Firebase Firestore.
 */
interface CloudSyncManager {
    suspend fun backupCatalog(userId: String, jsonString: String): Result<Unit>
    suspend fun restoreCatalog(userId: String): Result<String>
    suspend fun updateUsername(userId: String, newUsername: String): Result<Unit>
    suspend fun updateProfilePhoto(userId: String, photoData: String): Result<Unit>
    suspend fun pickProfilePhoto(): Result<String?>
    suspend fun fetchUsers(): Result<List<AdminUserModel>>
    suspend fun updateUserRole(userId: String, isAdmin: Boolean): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun fetchContributions(): Result<List<CommunityPostModel>>
    suspend fun publishContribution(song: SongItemModel, authorId: String, authorName: String): Result<Unit>
    suspend fun approveContribution(id: String): Result<Unit>
    suspend fun deleteContribution(id: String): Result<Unit>
}

class NoOpCloudSyncManager : CloudSyncManager {
    override suspend fun backupCatalog(userId: String, jsonString: String): Result<Unit> = Result.success(Unit)
    override suspend fun restoreCatalog(userId: String): Result<String> = Result.failure(Exception("Cloud sync no disponible en modo offline"))
    override suspend fun updateUsername(userId: String, newUsername: String): Result<Unit> = Result.success(Unit)
    override suspend fun updateProfilePhoto(userId: String, photoData: String): Result<Unit> = Result.success(Unit)
    override suspend fun pickProfilePhoto(): Result<String?> = Result.failure(Exception("Selector no disponible en modo offline"))
    override suspend fun fetchUsers(): Result<List<AdminUserModel>> = Result.success(emptyList())
    override suspend fun updateUserRole(userId: String, isAdmin: Boolean): Result<Unit> = Result.failure(Exception("Sin conexión a Firestore"))
    override suspend fun deleteUser(userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun fetchContributions(): Result<List<CommunityPostModel>> = Result.failure(Exception("Sin conexión a Firestore"))
    override suspend fun publishContribution(song: SongItemModel, authorId: String, authorName: String): Result<Unit> = Result.failure(Exception("Sin conexión a Firestore"))
    override suspend fun approveContribution(id: String): Result<Unit> = Result.failure(Exception("Sin conexión a Firestore"))
    override suspend fun deleteContribution(id: String): Result<Unit> = Result.failure(Exception("Sin conexión a Firestore"))
}
