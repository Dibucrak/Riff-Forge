package com.riffforge.feature_contributions.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.feature_contributions.domain.model.Contribution
import com.riffforge.feature_contributions.domain.repository.CommunityRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CommunityRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CommunityRepository {

    private fun com.google.firebase.firestore.DocumentSnapshot.toContribution(): Contribution? {
        val approvedFlag = getBoolean("isApproved") ?: getBoolean("approved") ?: false
        val title = getString("title") ?: return null
        return Contribution(
            id = id,
            title = title,
            artist = getString("artist") ?: "",
            key = getString("key") ?: "C",
            tuning = getString("tuning") ?: "E Standard",
            bpm = (getLong("bpm") ?: 120L).toInt(),
            content = getString("content") ?: "",
            authorId = getString("authorId") ?: "",
            authorName = getString("authorName") ?: "Músico",
            isApproved = approvedFlag,
            timestamp = getLong("timestamp") ?: System.currentTimeMillis()
        )
    }

    override fun getApprovedContributions(): Flow<List<Contribution>> = callbackFlow {
        val subscription = firestore.collection("contributions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val remoteApproved = snapshot?.documents
                    ?.mapNotNull { it.toContribution() }
                    ?.filter { it.isApproved }
                    ?.sortedByDescending { it.timestamp }
                    ?: emptyList()
                trySend(remoteApproved)
            }

        awaitClose { subscription.remove() }
    }

    override fun getContributionsByAuthor(authorId: String): Flow<List<Contribution>> = callbackFlow {
        if (authorId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = firestore.collection("contributions")
            .whereEqualTo("authorId", authorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toContribution() }
                    ?.sortedByDescending { it.timestamp }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    override fun getAllContributions(): Flow<List<Contribution>> = callbackFlow {
        val subscription = firestore.collection("contributions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toContribution() }
                    ?.sortedByDescending { it.timestamp }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun publishContribution(contribution: Contribution): Result<Unit> {
        return try {
            val document = firestore.collection("contributions").document()
            val payload = hashMapOf(
                "id" to document.id,
                "title" to contribution.title.trim().ifBlank { "Sin título" }.take(200),
                "artist" to contribution.artist.trim().ifBlank { "Original" }.take(200),
                "key" to contribution.key.trim().ifBlank { "C" }.take(50),
                "tuning" to contribution.tuning.trim().ifBlank { "E Standard" }.take(100),
                "bpm" to contribution.bpm.coerceIn(0, 500),
                "content" to contribution.content.trim().ifBlank { "Sin letra ni acordes" }.take(50000),
                "authorId" to contribution.authorId,
                "authorName" to contribution.authorName.trim().ifBlank { "Músico Riff Forge" }.take(100),
                "isApproved" to false,
                "approved" to false,
                "timestamp" to System.currentTimeMillis()
            )
            document.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPendingContributions(): Flow<List<Contribution>> = callbackFlow {
        val subscription = firestore.collection("contributions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val pending = snapshot.documents
                        .mapNotNull { it.toContribution() }
                        .filter { !it.isApproved }
                        .sortedBy { it.timestamp }
                    trySend(pending)
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun approveContribution(id: String): Result<Unit> {
        return try {
            firestore.collection("contributions").document(id).update(
                mapOf(
                    "isApproved" to true,
                    "approved" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectContribution(id: String): Result<Unit> {
        return deleteContribution(id)
    }

    override suspend fun deleteContribution(id: String): Result<Unit> {
        return try {
            firestore.collection("contributions").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
