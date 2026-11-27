package com.riffforge.feature_contributions.presentation.community_explorer

import com.riffforge.feature_contributions.domain.model.Contribution
import com.riffforge.feature_songs.domain.model.Song

sealed class CommunityEvent {
    data class SearchQueryChanged(val query: String) : CommunityEvent()
    data class SelectTab(val index: Int) : CommunityEvent()
    data class DownloadContribution(val contribution: Contribution) : CommunityEvent()
    data class DeleteMyContribution(val id: String) : CommunityEvent()
    data class ToggleExpandContribution(val id: String) : CommunityEvent()
    object ShowShareSongDialog : CommunityEvent()
    object HideShareSongDialog : CommunityEvent()
    data class PublishLocalSong(val song: Song) : CommunityEvent()
}
