package com.riffforge.feature_setlists.presentation.setlist_detail

sealed class SetlistDetailEvent {
    object ShowAddSongDialog : SetlistDetailEvent()
    object HideAddSongDialog : SetlistDetailEvent()
    data class AddSongToSetlist(val songId: Int) : SetlistDetailEvent()
    data class RemoveSongFromSetlist(val songId: Int) : SetlistDetailEvent()
}