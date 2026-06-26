package com.riffforge.feature_setlists.domain.model

import com.riffforge.feature_songs.domain.model.Song

data class SetlistDetail(
    val setlist: Setlist,
    val songs: List<Song>
)