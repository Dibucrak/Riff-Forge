package com.riffforge.feature_contributions.presentation.community_explorer

import com.riffforge.feature_contributions.domain.model.Contribution
import com.riffforge.feature_songs.domain.model.Song

data class CommunityState(
    val contributions: List<Contribution> = emptyList(),
    val myContributions: List<Contribution> = emptyList(),
    val mySongs: List<Song> = emptyList(),
    val authorAvatars: Map<String, Pair<String, String>> = emptyMap(),
    val selectedTab: Int = 0,
    val downloadedIds: Set<String> = emptySet(),
    val expandedIds: Set<String> = emptySet(),
    val isShareDialogVisible: Boolean = false,
    val isLoading: Boolean = true,
    val error: String = "",
    val searchQuery: String = ""
)
