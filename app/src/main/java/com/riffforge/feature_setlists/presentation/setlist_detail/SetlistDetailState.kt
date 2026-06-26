package com.riffforge.feature_setlists.presentation.setlist_detail

import com.riffforge.feature_setlists.domain.model.SetlistDetail
import com.riffforge.feature_songs.domain.model.Song

data class SetlistDetailState(
    val setlistDetail: SetlistDetail? = null,
    val allSongs: List<Song> = emptyList(),
    val isAddingSong: Boolean = false,
    val isLoading: Boolean = true
)