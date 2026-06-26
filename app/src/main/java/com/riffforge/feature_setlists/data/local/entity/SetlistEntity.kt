package com.riffforge.feature_setlists.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setlists")
data class SetlistEntity(
    @PrimaryKey(autoGenerate = true)
    val setId: Int = 0,
    val name: String,
    val description: String
)