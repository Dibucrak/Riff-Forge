package com.riffforge.feature_songs.data.repository

import com.riffforge.feature_songs.data.local.dao.SongDao
import com.riffforge.feature_songs.data.local.entity.SongEntity
import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun SongEntity.toDomain(): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        key = key,
        tuning = tuning,
        bpm = bpm,
        content = content,
        isDraft = isDraft
    )
}

fun Song.toEntity(): SongEntity {
    return SongEntity(
        id = id,
        title = title,
        artist = artist,
        key = key,
        tuning = tuning,
        bpm = bpm,
        content = content,
        isDraft = isDraft
    )
}

class SongRepositoryImpl(
    private val dao: SongDao
) : SongRepository {

    override fun getSongs(): Flow<List<Song>> {
        return dao.getAllSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSongById(id: Int): Song? {
        return dao.getSongById(id)?.toDomain()
    }

    override suspend fun insertSong(song: Song) {
        dao.insertSong(song.toEntity())
    }
}