package com.riffforge.feature_cloud_backup.presentation

sealed class CloudBackupEvent {
    object BackupToCloud : CloudBackupEvent()
    object RestoreFromCloud : CloudBackupEvent()
}
