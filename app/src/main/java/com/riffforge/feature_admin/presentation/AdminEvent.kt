package com.riffforge.feature_admin.presentation

sealed class AdminEvent {
    data class ApproveContribution(val id: String) : AdminEvent()
    data class RejectContribution(val id: String) : AdminEvent()
}