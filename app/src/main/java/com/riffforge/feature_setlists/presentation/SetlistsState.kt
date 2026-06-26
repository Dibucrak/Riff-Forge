package com.riffforge.feature_setlists.presentation

import com.riffforge.feature_setlists.domain.model.SetlistDetail

data class SetlistsState(
    val setlists: List<SetlistDetail> = emptyList(),
    val isAddingSetlist: Boolean = false,
    val newSetlistName: String = "",
    val newSetlistDescription: String = ""
)