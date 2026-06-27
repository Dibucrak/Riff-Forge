package com.riffforge.feature_profile.presentation

sealed class ProfileEvent {
    object SignOut : ProfileEvent()
    object BackupToCloud : ProfileEvent()
    object RestoreFromCloud : ProfileEvent()
}