package com.riffforge.feature_setlists.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "setlist_song_cross_ref",
    primaryKeys = ["setId", "songId"]
)
data class SetlistSongCrossRef(
    val setId: Int,
    val songId: Int
)