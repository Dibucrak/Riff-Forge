package com.riffforge.feature_admin.presentation

sealed class AdminEvent {
    data class ApproveContribution(val id: String) : AdminEvent()
    data class RejectContribution(val id: String) : AdminEvent()
    data class SelectTab(val index: Int) : AdminEvent()
    data class ToggleAdminRole(val uid: String, val isAdmin: Boolean) : AdminEvent()
    data class DeleteUser(val uid: String) : AdminEvent()
    object ConfirmDeleteUser : AdminEvent()
    data class SetDeleteTarget(val uid: String) : AdminEvent()
}