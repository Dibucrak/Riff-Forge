package com.riffforge.feature_song_editor.presentation

sealed class AddEditSongEvent {
    data class EnteredTitle(val value: String) : AddEditSongEvent()
    data class EnteredArtist(val value: String) : AddEditSongEvent()
    data class EnteredKey(val value: String) : AddEditSongEvent()
    data class EnteredTuning(val value: String) : AddEditSongEvent()
    data class EnteredBpm(val value: String) : AddEditSongEvent()
    data class EnteredContent(val value: String) : AddEditSongEvent()
    object SaveSong : AddEditSongEvent()
    object PublishToCommunity : AddEditSongEvent()
}