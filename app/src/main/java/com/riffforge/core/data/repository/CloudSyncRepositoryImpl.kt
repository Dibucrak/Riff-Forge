package com.riffforge.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.core.domain.model.CloudBackupData
import com.riffforge.core.domain.repository.CloudSyncRepository
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
                Result.success(CloudBackupData())
            } else {
                Result.failure(Exception("No se encontró ningún respaldo en la nube."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}