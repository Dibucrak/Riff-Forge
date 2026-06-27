package com.riffforge.feature_contributions.presentation.community_explorer

import com.riffforge.feature_contributions.domain.model.Contribution

data class CommunityState(
    val contributions: List<Contribution> = emptyList(),
    val isLoading: Boolean = true,
    val error: String = "",
    val searchQuery: String = ""
)