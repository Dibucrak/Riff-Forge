package com.riffforge.feature_setlists.data.repository

import com.riffforge.feature_setlists.data.local.dao.SetlistDao
import com.riffforge.feature_setlists.data.local.entity.SetlistEntity
import com.riffforge.feature_setlists.data.local.entity.SetlistSongCrossRef
import com.riffforge.feature_setlists.domain.model.Setlist
import com.riffforge.feature_setlists.domain.model.SetlistDetail
import com.riffforge.feature_setlists.domain.repository.SetlistRepository
import com.riffforge.feature_songs.data.repository.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun SetlistEntity.toDomain(): Setlist {
    return Setlist(
        id = setId,
        name = name,
        description = description
    )
}

fun Setlist.toEntity(): SetlistEntity {
    return SetlistEntity(
        setId = id,
        name = name,
        description = description
    )
}

class SetlistRepositoryImpl(
    private val dao: SetlistDao
) : SetlistRepository {

    override fun getSetlists(): Flow<List<SetlistDetail>> {
        return dao.getSetlistsWithSongs().map { relations ->
            relations.map { relation ->
                SetlistDetail(
                    setlist = relation.setlist.toDomain(),
                    songs = relation.songs.map { it.toDomain() }
                )
            }
        }
    }

    override fun getSetlistById(id: Int): Flow<SetlistDetail?> {
        return dao.getSetlistById(id).map { relation ->
            relation?.let {
                SetlistDetail(
                    setlist = it.setlist.toDomain(),
                    songs = it.songs.map { song -> song.toDomain() }
                )
            }
        }
    }

    override suspend fun insertSetlist(setlist: Setlist): Long {
        return dao.insertSetlist(setlist.toEntity())
    }

    override suspend fun addSongToSetlist(setId: Int, songId: Int) {
        dao.insertSetlistSongCrossRef(SetlistSongCrossRef(setId, songId))
    }

    override suspend fun removeSongFromSetlist(setId: Int, songId: Int) {
        dao.removeSongFromSetlist(setId, songId)
    }
}