package com.riffforge.feature_contributions.presentation.community_explorer

import com.riffforge.feature_contributions.domain.model.Contribution

sealed class CommunityEvent {
    data class SearchQueryChanged(val query: String) : CommunityEvent()
    data class DownloadContribution(val contribution: Contribution) : CommunityEvent()
}