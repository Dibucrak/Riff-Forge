package com.riffforge.feature_setlists.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "setlist_song_cross_ref",
    primaryKeys = ["setId", "songId"],
    indices = [Index(value = ["songId"])]
)
data class SetlistSongCrossRef(
    val setId: Int,
    val songId: Int
)