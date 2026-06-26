package com.riffforge.feature_setlists.presentation

sealed class SetlistsEvent {
    object ShowAddSetlistDialog : SetlistsEvent()
    object HideAddSetlistDialog : SetlistsEvent()
    data class EnteredName(val name: String) : SetlistsEvent()
    data class EnteredDescription(val description: String) : SetlistsEvent()
    object SaveSetlist : SetlistsEvent()
}