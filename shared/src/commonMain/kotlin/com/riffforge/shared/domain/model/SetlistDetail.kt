package com.riffforge.shared.domain.model

data class SetlistDetail(
    val setlist: Setlist,
    val songs: List<Song>
)
