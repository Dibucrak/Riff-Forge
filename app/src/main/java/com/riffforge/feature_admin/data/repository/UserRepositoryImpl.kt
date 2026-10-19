package com.riffforge.feature_admin.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.feature_admin.domain.model.AdminUser
import com.riffforge.feature_admin.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")
    private val contributionsCollection = firestore.collection("contributions")

    override fun getAllUsers(): Flow<List<AdminUser>> = callbackFlow {
        val subscription = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val users = snapshot?.documents?.mapNotNull { doc ->
                val uid = doc.id
                val email = doc.getString("email") ?: ""
                val displayName = doc.getString("displayName")
                val isAdmin = doc.getBoolean("isAdmin") ?: false
                val joinDate = doc.getLong("joinDate") ?: System.currentTimeMillis()

                AdminUser(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    isAdmin = isAdmin,
                    joinDate = joinDate
                )
            } ?: emptyList()

            trySend(users)
        }

        awaitClose { subscription.remove() }
    }

    override suspend fun updateAdminRole(uid: String, isAdmin: Boolean): Result<Unit> {
        return try {
            usersCollection.document(uid).update("isAdmin", isAdmin).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            // Eliminar el documento de usuario en Firestore
            usersCollection.document(uid).delete().await()
            // Eliminar la subcolección de backups
            val backups = usersCollection.document(uid).collection("backups").get().await()
            for (doc in backups.documents) {
                doc.reference.delete().await()
            }
            // Eliminar contributions del usuario
            val contributions = contributionsCollection.whereEqualTo("authorId", uid).get().await()
            for (doc in contributions.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
