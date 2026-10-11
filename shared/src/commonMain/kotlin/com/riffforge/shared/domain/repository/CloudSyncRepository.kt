package com.riffforge.shared.domain.repository

import com.riffforge.shared.domain.model.CloudBackupData

interface CloudSyncRepository {
    suspend fun backupCatalog(userId: String, data: CloudBackupData): Result<Unit>
    suspend fun restoreCatalog(userId: String): Result<CloudBackupData>
}
