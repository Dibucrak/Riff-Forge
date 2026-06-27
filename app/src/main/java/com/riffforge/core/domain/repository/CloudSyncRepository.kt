package com.riffforge.core.domain.repository

import com.riffforge.core.domain.model.CloudBackupData

interface CloudSyncRepository {
    suspend fun backupCatalog(userId: String, data: CloudBackupData): Result<Unit>
    suspend fun restoreCatalog(userId: String): Result<CloudBackupData>
}