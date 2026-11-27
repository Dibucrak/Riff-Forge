package com.riffforge.core.domain.store

import androidx.compose.runtime.mutableStateListOf
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.abs

@Serializable
data class CloudSongDto(
    val id: Int = 0,
    val title: String = "",
    val artist: String = "",
    val key: String = "",
    val tuning: String = "E Standard",
    val bpm: Int = 120,
    val content: String = "",
    val isDraft: Boolean = false
)

@Serializable
data class CloudSetlistDetailDto(
    val id: Int = 0,
    val name: String = "",
    val description: String = ""
)

@Serializable
data class CloudSetlistDto(
    val setlist: CloudSetlistDetailDto = CloudSetlistDetailDto(),
    val songs: List<Int> = emptyList()
)

@Serializable
data class CloudCatalogBackupDto(
    val songs: List<CloudSongDto> = emptyList(),
    val setlists: List<CloudSetlistDto> = emptyList()
)

@Serializable
data class SongItemModel(
    val id: String,
    val title: String,
    val artist: String,
    val bpm: Int,
    val key: String,
    val tuning: String = "E Standard",
    val lyricsAndChords: String
)

@Serializable
data class SetlistItemModel(
    val id: String,
    val name: String,
    val description: String,
    val songIds: List<String> = emptyList()
)

@Serializable
data class CommunityPostModel(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorPhotoUrl: String = "",
    val authorPhotoData: String = "",
    val bpm: Int = 120,
    val key: String = "C",
    val tuning: String = "E Standard",
    val lyricsAndChords: String = "",
    val status: String = "PENDING",
    val timestamp: Long = 0L
)

@Serializable
data class AdminUserModel(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "USER",
    val photoUrl: String = "",
    val photoData: String = ""
)

object RepertoireStore {
    private val settings = Settings()
    private val jsonCodec = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        isLenient = true
    }

    private const val KEY_LOCAL_SONGS = "riffforge_local_songs_v1"
    private const val KEY_LOCAL_SETLISTS = "riffforge_local_setlists_v1"

    val songs = mutableStateListOf<SongItemModel>()
    val setlists = mutableStateListOf<SetlistItemModel>()
    val contributions = mutableStateListOf<CommunityPostModel>()
    val users = mutableStateListOf<AdminUserModel>()

    init {
        loadLocalData()
    }

    private fun loadLocalData() {
        val rawSongs = settings.get(KEY_LOCAL_SONGS, "")
        if (rawSongs.isNotBlank()) {
            runCatching {
                jsonCodec.decodeFromString<List<SongItemModel>>(rawSongs)
            }.getOrNull()?.let { decoded ->
                songs.clear()
                songs.addAll(decoded)
            }
        }
        val rawSetlists = settings.get(KEY_LOCAL_SETLISTS, "")
        if (rawSetlists.isNotBlank()) {
            runCatching {
                jsonCodec.decodeFromString<List<SetlistItemModel>>(rawSetlists)
            }.getOrNull()?.let { decoded ->
                setlists.clear()
                setlists.addAll(decoded)
            }
        }
        ensureUniqueIds()
    }

    private fun persistLocalData() {
        runCatching {
            settings.set(KEY_LOCAL_SONGS, jsonCodec.encodeToString(songs.toList()))
            settings.set(KEY_LOCAL_SETLISTS, jsonCodec.encodeToString(setlists.toList()))
        }
    }

    fun clearLocalRepertoire() {
        songs.clear()
        setlists.clear()
        persistLocalData()
    }

    fun getSong(songId: String?): SongItemModel? {
        if (songId.isNullOrBlank()) return null
        val cleanId = songId.trim()
        return songs.find { it.id.trim() == cleanId }
            ?: songs.find { it.id.toIntOrNull() != null && it.id.toIntOrNull() == cleanId.toIntOrNull() }
            ?: songs.find { it.title.trim().equals(cleanId, ignoreCase = true) }
    }

    fun ensureUniqueIds() {
        val seen = mutableSetOf<String>()
        for (i in 0 until songs.size) {
            val item = songs[i]
            if (item.id.isBlank() || seen.contains(item.id)) {
                var counter = i + 1
                var candidate = "song_$counter"
                while (seen.contains(candidate) || songs.any { it.id == candidate }) {
                    counter++
                    candidate = "song_$counter"
                }
                songs[i] = item.copy(id = candidate)
                seen.add(candidate)
            } else {
                seen.add(item.id)
            }
        }
    }

    fun getSetlist(setlistId: String?): SetlistItemModel? {
        if (setlistId.isNullOrBlank()) return null
        val cleanId = setlistId.trim()
        return setlists.find { it.id.trim() == cleanId }
            ?: setlists.find { it.id.toIntOrNull() != null && it.id.toIntOrNull() == cleanId.toIntOrNull() }
            ?: setlists.find { it.name.trim().equals(cleanId, ignoreCase = true) }
    }

    fun getSongsForSetlist(setlistId: String?): List<SongItemModel> {
        val setlist = getSetlist(setlistId) ?: return emptyList()
        return setlist.songIds.mapNotNull { id ->
            val cleanId = id.trim()
            songs.find { it.id.trim() == cleanId }
                ?: songs.find { it.id.toIntOrNull() != null && it.id.toIntOrNull() == cleanId.toIntOrNull() }
                ?: songs.find { it.title.trim().equals(cleanId, ignoreCase = true) }
        }
    }

    fun saveSong(
        id: String?,
        title: String,
        artist: String,
        key: String,
        tuning: String,
        bpm: Int,
        lyricsAndChords: String
    ): SongItemModel {
        val songId = id?.takeIf { it.isNotBlank() } ?: run {
            var counter = songs.size + 1
            while (songs.any { it.id == "song_$counter" || it.id == counter.toString() }) {
                counter++
            }
            "song_$counter"
        }
        val model = SongItemModel(
            id = songId,
            title = title.trim().ifBlank { "Sin Título" },
            artist = artist.trim().ifBlank { "Artista Desconocido" },
            bpm = bpm.coerceIn(30, 300),
            key = key.ifBlank { "C" },
            tuning = tuning.ifBlank { "E Standard" },
            lyricsAndChords = lyricsAndChords
        )
        val index = songs.indexOfFirst { it.id == songId }
        if (index >= 0) {
            songs[index] = model
        } else {
            songs.add(0, model)
        }
        ensureUniqueIds()
        persistLocalData()
        return model
    }

    fun deleteSong(songId: String) {
        songs.removeAll { it.id == songId }
        setlists.forEachIndexed { index, sl ->
            if (sl.songIds.contains(songId)) {
                setlists[index] = sl.copy(songIds = sl.songIds.filter { it != songId })
            }
        }
        persistLocalData()
    }

    fun addSetlist(name: String, description: String) {
        if (name.isBlank()) return
        var counter = setlists.size + 1
        while (setlists.any { it.id == "setlist_$counter" || it.id == counter.toString() }) {
            counter++
        }
        setlists.add(
            0,
            SetlistItemModel(
                id = "setlist_$counter",
                name = name.trim(),
                description = description.trim()
            )
        )
        persistLocalData()
    }

    fun updateSetlist(setlistId: String, name: String, description: String) {
        if (name.isBlank()) return
        val index = setlists.indexOfFirst { it.id == setlistId }
        if (index >= 0) {
            setlists[index] = setlists[index].copy(
                name = name.trim(),
                description = description.trim()
            )
            persistLocalData()
        }
    }

    fun deleteSetlist(setlistId: String) {
        setlists.removeAll { it.id == setlistId }
        persistLocalData()
    }

    fun addSongToSetlist(setlistId: String, songId: String) {
        val index = setlists.indexOfFirst { it.id == setlistId }
        if (index >= 0) {
            val current = setlists[index]
            if (!current.songIds.contains(songId)) {
                setlists[index] = current.copy(songIds = current.songIds + songId)
                persistLocalData()
            }
        }
    }

    fun removeSongFromSetlist(setlistId: String, songId: String) {
        val index = setlists.indexOfFirst { it.id == setlistId }
        if (index >= 0) {
            val current = setlists[index]
            setlists[index] = current.copy(songIds = current.songIds.filter { it != songId })
            persistLocalData()
        }
    }

    fun isContributionImported(contribution: CommunityPostModel): Boolean {
        return songs.any {
            it.title.trim().equals(contribution.title.trim(), ignoreCase = true) &&
                it.artist.trim().equals(contribution.artist.trim(), ignoreCase = true)
        }
    }

    fun submitContributionFromSong(
        song: SongItemModel,
        authorUsername: String = "riffforge_user",
        authorId: String = ""
    ) {
        var counter = contributions.size + 1
        while (contributions.any { it.id == "comm_$counter" }) {
            counter++
        }
        val newContribution = CommunityPostModel(
            id = "comm_$counter",
            title = song.title,
            artist = song.artist,
            authorId = authorId,
            authorUsername = authorUsername,
            bpm = song.bpm,
            key = song.key,
            tuning = song.tuning,
            lyricsAndChords = song.lyricsAndChords,
            status = "PENDING"
        )
        contributions.add(0, newContribution)
    }

    fun importContributionToRepertoire(contribution: CommunityPostModel): Boolean {
        if (isContributionImported(contribution)) return false
        var candidateId = "dl_${contribution.id.ifBlank { abs(contribution.title.hashCode()).toString() }}"
        var counter = 1
        while (songs.any { it.id == candidateId }) {
            candidateId = "dl_${contribution.id}_$counter"
            counter++
        }
        songs.add(
            0,
            SongItemModel(
                id = candidateId,
                title = contribution.title.ifBlank { "Sin Título" },
                artist = contribution.artist.ifBlank { "Artista Desconocido" },
                bpm = contribution.bpm.coerceIn(30, 300),
                key = contribution.key.ifBlank { "C" },
                tuning = contribution.tuning.ifBlank { "E Standard" },
                lyricsAndChords = contribution.lyricsAndChords
            )
        )
        persistLocalData()
        return true
    }

    fun setContributions(items: List<CommunityPostModel>) {
        contributions.clear()
        contributions.addAll(items)
    }

    fun updateContributionStatus(id: String, status: String) {
        val index = contributions.indexOfFirst { it.id == id }
        if (index >= 0) {
            contributions[index] = contributions[index].copy(status = status)
        }
    }

    fun deleteContribution(id: String) {
        contributions.removeAll { it.id == id }
    }

    fun updateUserRole(uid: String, newRole: String) {
        val index = users.indexOfFirst { it.uid == uid }
        if (index >= 0) {
            users[index] = users[index].copy(role = newRole)
        }
    }

    fun deleteUser(uid: String) {
        users.removeAll { it.uid == uid }
    }

    fun setUsers(newUsers: List<AdminUserModel>) {
        users.clear()
        users.addAll(newUsers)
    }

    /**
     * Serializes current songs and setlists to Firestore-compatible JSON format,
     * preserving setlist song references even for non-numeric local IDs.
     */
    fun exportBackupJson(): String {
        val usedIntIds = mutableSetOf<Int>()
        val songIdToIntMap = mutableMapOf<String, Int>()
        var nextGeneratedId = 1

        val songDtos = songs.map { song ->
            val parsed = song.id.toIntOrNull()?.takeIf { it > 0 && !usedIntIds.contains(it) }
            val assignedIntId = parsed ?: run {
                while (usedIntIds.contains(nextGeneratedId)) {
                    nextGeneratedId++
                }
                nextGeneratedId
            }
            usedIntIds.add(assignedIntId)
            songIdToIntMap[song.id] = assignedIntId

            CloudSongDto(
                id = assignedIntId,
                title = song.title,
                artist = song.artist,
                key = song.key,
                tuning = song.tuning,
                bpm = song.bpm,
                content = song.lyricsAndChords,
                isDraft = false
            )
        }

        val usedSetlistIds = mutableSetOf<Int>()
        var nextSetId = 1
        val setlistDtos = setlists.map { setlist ->
            val parsedSetId = setlist.id.toIntOrNull()?.takeIf { it > 0 && !usedSetlistIds.contains(it) }
            val assignedSetId = parsedSetId ?: run {
                while (usedSetlistIds.contains(nextSetId)) {
                    nextSetId++
                }
                nextSetId
            }
            usedSetlistIds.add(assignedSetId)

            CloudSetlistDto(
                setlist = CloudSetlistDetailDto(
                    id = assignedSetId,
                    name = setlist.name,
                    description = setlist.description
                ),
                songs = setlist.songIds.mapNotNull { localSongId ->
                    songIdToIntMap[localSongId] ?: localSongId.toIntOrNull()
                }
            )
        }

        val catalog = CloudCatalogBackupDto(songs = songDtos, setlists = setlistDtos)
        return Json { prettyPrint = false; ignoreUnknownKeys = true }.encodeToString(catalog)
    }

    /**
     * Restores songs and setlists from Firestore-compatible JSON format.
     * Returns Pair(number of songs restored/updated, number of setlists restored).
     */
    fun importBackupJson(jsonString: String): Pair<Int, Int> {
        val catalog = jsonCodec.decodeFromString<CloudCatalogBackupDto>(jsonString)

        var newSongsCount = 0
        var updatedSongsCount = 0

        val existingIds = songs.map { it.id }.toMutableSet()
        val cloudIntIdToLocalId = mutableMapOf<Int, String>()

        catalog.songs.forEach { cloudSong ->
            val existingIndex = songs.indexOfFirst {
                it.title.trim().equals(cloudSong.title.trim(), ignoreCase = true) &&
                    it.artist.trim().equals(cloudSong.artist.trim(), ignoreCase = true)
            }
            if (existingIndex >= 0) {
                val existingSong = songs[existingIndex]
                val model = existingSong.copy(
                    bpm = if (cloudSong.bpm > 0) cloudSong.bpm else existingSong.bpm,
                    key = if (cloudSong.key.isNotBlank()) cloudSong.key else existingSong.key,
                    tuning = if (cloudSong.tuning.isNotBlank()) cloudSong.tuning else existingSong.tuning,
                    lyricsAndChords = if (cloudSong.content.isNotBlank()) cloudSong.content else existingSong.lyricsAndChords
                )
                songs[existingIndex] = model
                if (cloudSong.id > 0) cloudIntIdToLocalId[cloudSong.id] = existingSong.id
                updatedSongsCount++
            } else {
                var candidateId = if (cloudSong.id > 0) cloudSong.id.toString() else "song_${songs.size + 1}"
                if (existingIds.contains(candidateId)) {
                    var counter = 1
                    while (existingIds.contains("${candidateId}_$counter")) {
                        counter++
                    }
                    candidateId = "${candidateId}_$counter"
                }
                existingIds.add(candidateId)

                val model = SongItemModel(
                    id = candidateId,
                    title = cloudSong.title.ifBlank { "Sin Título" },
                    artist = cloudSong.artist.ifBlank { "Artista Desconocido" },
                    bpm = if (cloudSong.bpm > 0) cloudSong.bpm else 120,
                    key = if (cloudSong.key.isNotBlank()) cloudSong.key else "C",
                    tuning = if (cloudSong.tuning.isNotBlank()) cloudSong.tuning else "E Standard",
                    lyricsAndChords = cloudSong.content
                )
                songs.add(model)
                if (cloudSong.id > 0) cloudIntIdToLocalId[cloudSong.id] = candidateId
                newSongsCount++
            }
        }
        ensureUniqueIds()

        var restoredSetlistsCount = 0
        catalog.setlists.forEach { cloudSetlist ->
            val existingIndex = setlists.indexOfFirst {
                it.name.trim().equals(cloudSetlist.setlist.name.trim(), ignoreCase = true)
            }
            val mappedSongIds = cloudSetlist.songs.mapNotNull { cloudSongId ->
                cloudIntIdToLocalId[cloudSongId]
                    ?: songs.find { it.id == cloudSongId.toString() }?.id
                    ?: catalog.songs.find { it.id == cloudSongId }?.let { catalogSong ->
                        songs.find { it.title.trim().equals(catalogSong.title.trim(), ignoreCase = true) }?.id
                    }
            }

            val targetSetlistId = if (existingIndex >= 0) {
                setlists[existingIndex].id
            } else {
                val base = if (cloudSetlist.setlist.id > 0) cloudSetlist.setlist.id.toString() else "setlist_${setlists.size + 1}"
                if (setlists.any { it.id == base }) "setlist_${base}_${setlists.size + 1}" else base
            }

            val model = SetlistItemModel(
                id = targetSetlistId,
                name = cloudSetlist.setlist.name,
                description = cloudSetlist.setlist.description,
                songIds = mappedSongIds
            )
            if (existingIndex >= 0) {
                setlists[existingIndex] = model
            } else {
                setlists.add(model)
            }
            restoredSetlistsCount++
        }

        persistLocalData()
        return Pair(newSongsCount + updatedSongsCount, restoredSetlistsCount)
    }
}
