package com.riffforge.feature_admin.presentation

import com.riffforge.feature_contributions.domain.model.Contribution

data class AdminState(
    val pendingContributions: List<Contribution> = emptyList(),
    val isLoading: Boolean = true
)