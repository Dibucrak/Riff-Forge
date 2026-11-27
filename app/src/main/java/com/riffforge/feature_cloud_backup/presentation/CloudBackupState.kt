package com.riffforge.feature_cloud_backup.presentation

data class CloudBackupState(
    val isSyncing: Boolean = false,
    val uid: String = "",
    val songCount: Int = 0,
    val setlistCount: Int = 0,
    val lastSyncLabel: String = ""
)
