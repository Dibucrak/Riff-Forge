package com.riffforge.feature_setlists.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.riffforge.feature_setlists.data.local.entity.SetlistEntity
import com.riffforge.feature_setlists.data.local.entity.SetlistSongCrossRef
import com.riffforge.feature_songs.data.local.entity.SongEntity

data class SetlistWithSongsRelation(
    @Embedded val setlist: SetlistEntity,
    @Relation(
        parentColumn = "setId",
        entityColumn = "id",
        associateBy = Junction(
            value = SetlistSongCrossRef::class,
            parentColumn = "setId",
            entityColumn = "songId"
        )
    )
    val songs: List<SongEntity>
)