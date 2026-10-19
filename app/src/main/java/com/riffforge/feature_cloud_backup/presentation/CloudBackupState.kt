package com.riffforge.feature_cloud_backup.presentation

data class CloudBackupState(
    val isSyncing: Boolean = false,
    val uid: String = ""
)
