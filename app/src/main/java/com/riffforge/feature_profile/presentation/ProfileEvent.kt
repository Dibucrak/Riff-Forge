package com.riffforge.feature_profile.presentation

import android.app.Activity

sealed class ProfileEvent {
    object SignOut : ProfileEvent()
    object DeleteAccount : ProfileEvent()
    data class LinkWithGoogle(val activity: Activity) : ProfileEvent()
    object BackupToCloud : ProfileEvent()
    object RestoreFromCloud : ProfileEvent()
    data class ToggleReminder(val isEnabled: Boolean) : ProfileEvent()
    data class UpdateUsername(val newUsername: String) : ProfileEvent()
    data class UpdateProfilePhoto(val photoBase64: String) : ProfileEvent()
    object ResetProfilePhoto : ProfileEvent()
}
