package com.riffforge.feature_contributions.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.riffforge.feature_contributions.domain.model.Contribution
import com.riffforge.feature_contributions.domain.repository.CommunityRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CommunityRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CommunityRepository {

    override fun getApprovedContributions(): Flow<List<Contribution>> = callbackFlow {
        val subscription = firestore.collection("contributions")
            .whereEqualTo("isApproved", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val contributions = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Contribution::class.java)?.copy(id = doc.id)
                    }
                    trySend(contributions)
                }
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun publishContribution(contribution: Contribution): Result<Unit> {
        return try {
            val document = firestore.collection("contributions").document()
            val newContribution = contribution.copy(id = document.id)

            document.set(newContribution).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}