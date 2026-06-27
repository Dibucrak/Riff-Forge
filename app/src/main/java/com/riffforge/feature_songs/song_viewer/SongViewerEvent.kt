package com.riffforge.feature_songs.presentation.song_viewer

sealed class SongViewerEvent {
    object ToggleAutoScroll : SongViewerEvent()
    object IncreaseSpeed : SongViewerEvent()
    object DecreaseSpeed : SongViewerEvent()
    object IncreaseTextSize : SongViewerEvent()
    object DecreaseTextSize : SongViewerEvent()

    object TransposeUp : SongViewerEvent()
    object TransposeDown : SongViewerEvent()
    object CapoUp : SongViewerEvent()
    object CapoDown : SongViewerEvent()
    object ToggleFlats : SongViewerEvent()
}