package com.riffforge.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.core.domain.model.CloudBackupData
import com.riffforge.core.domain.repository.CloudSyncRepository
import com.riffforge.feature_setlists.domain.model.Setlist
import com.riffforge.feature_setlists.domain.model.SetlistDetail
import com.riffforge.feature_songs.domain.model.Song
import kotlinx.coroutines.tasks.await

class CloudSyncRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CloudSyncRepository {

    override suspend fun backupCatalog(userId: String, data: CloudBackupData): Result<Unit> {
        return try {
            val backupMap = hashMapOf(
                "songs" to data.songs.map { song ->
                    hashMapOf(
                        "id" to song.id,
                        "title" to song.title,
                        "artist" to song.artist,
                        "key" to song.key,
                        "tuning" to song.tuning,
                        "bpm" to song.bpm,
                        "content" to song.content,
                        "isDraft" to song.isDraft
                    )
                },
                "setlists" to data.setlists.map { setlistDetail ->
                    hashMapOf(
                        "setlist" to hashMapOf(
                            "id" to setlistDetail.setlist.id,
                            "name" to setlistDetail.setlist.name,
                            "description" to setlistDetail.setlist.description
                        ),
                        "songs" to setlistDetail.songs.map { it.id }
                    )
                }
            )

            firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .set(backupMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreCatalog(userId: String): Result<CloudBackupData> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .get()
                .await()

            if (snapshot.exists()) {
                val data = snapshot.data ?: return Result.failure(Exception("Datos vacíos en la nube."))

                val songsData = data["songs"] as? List<Map<String, Any>> ?: emptyList()
                val parsedSongs = songsData.map { map ->
                    Song(
                        id = (map["id"] as? Long)?.toInt() ?: 0,
                        title = map["title"] as? String ?: "",
                        artist = map["artist"] as? String ?: "",
                        key = map["key"] as? String ?: "",
                        tuning = map["tuning"] as? String ?: "",
                        bpm = (map["bpm"] as? Long)?.toInt() ?: 120,
                        content = map["content"] as? String ?: "",
                        isDraft = map["isDraft"] as? Boolean ?: false
                    )
                }

                val setlistsData = data["setlists"] as? List<Map<String, Any>> ?: emptyList()
                val parsedSetlists = setlistsData.map { map ->
                    val setlistMap = map["setlist"] as? Map<String, Any> ?: emptyMap()
                    val songIds = map["songs"] as? List<Long> ?: emptyList()

                    val setlistSongs = songIds.mapNotNull { songId ->
                        parsedSongs.find { it.id == songId.toInt() }
                    }

                    SetlistDetail(
                        setlist = Setlist(
                            id = (setlistMap["id"] as? Long)?.toInt() ?: 0,
                            name = setlistMap["name"] as? String ?: "",
                            description = setlistMap["description"] as? String ?: ""
                        ),
                        songs = setlistSongs
                    )
                }

                Result.success(CloudBackupData(songs = parsedSongs, setlists = parsedSetlists))
            } else {
                Result.failure(Exception("No se encontró ningún respaldo en la nube."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}