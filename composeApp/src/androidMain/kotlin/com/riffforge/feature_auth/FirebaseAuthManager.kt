package com.riffforge.feature_auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.riffforge.core.domain.store.AdminUserModel
import com.riffforge.core.domain.store.CloudCatalogBackupDto
import com.riffforge.core.domain.store.CloudSetlistDetailDto
import com.riffforge.core.domain.store.CloudSetlistDto
import com.riffforge.core.domain.store.CloudSongDto
import com.riffforge.core.domain.store.CommunityPostModel
import com.riffforge.core.domain.store.SongItemModel
import com.riffforge.core.domain.sync.CloudSyncManager
import com.riffforge.feature_auth.domain.AuthManager
import com.riffforge.feature_auth.domain.AuthUser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Android implementation of AuthManager and CloudSyncManager using Firebase Auth + Firestore.
 */
class FirebaseAuthManager(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthManager, CloudSyncManager {

    private fun resolvePhotoUrl(user: com.google.firebase.auth.FirebaseUser): String {
        val raw = user.providerData
            .firstOrNull { it.providerId == "google.com" && it.photoUrl != null }
            ?.photoUrl?.toString()
            ?: user.photoUrl?.toString()
            ?: ""
        return raw.replace("=s96-c", "=s400-c")
    }

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val initialPhotoUrl = resolvePhotoUrl(firebaseUser)
                // Initial quick emission
                val quickUser = AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    username = firebaseUser.displayName?.lowercase()?.replace(" ", "_") ?: "",
                    isAdmin = false,
                    linkedProviders = firebaseUser.providerData.map { it.providerId },
                    photoUrl = initialPhotoUrl
                )
                trySend(quickUser)

                // Fetch details from Firestore asynchronously
                firestore.collection("users").document(firebaseUser.uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val username = doc.getString("username") ?: quickUser.username
                            val isAdmin = doc.getBoolean("isAdmin") ?: false
                            val storedPhotoUrl = doc.getString("photoUrl")?.takeIf { it.isNotBlank() } ?: initialPhotoUrl
                            val photoData = doc.getString("photoData") ?: ""
                            val pubPayload = hashMapOf<String, Any>(
                                "displayName" to (doc.getString("displayName") ?: quickUser.displayName).take(100),
                                "username" to username.take(60),
                                "photoUrl" to storedPhotoUrl.take(2048),
                                "photoData" to photoData.take(180000)
                            )
                            firestore.collection("public_profiles").document(firebaseUser.uid)
                                .set(pubPayload, SetOptions.merge())
                            trySend(
                                quickUser.copy(
                                    username = username,
                                    isAdmin = isAdmin,
                                    photoUrl = storedPhotoUrl,
                                    photoData = photoData
                                )
                            )
                        }
                    }
            } else {
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                var username = user.displayName?.lowercase()?.replace(" ", "_") ?: ""
                var isAdmin = false
                var photoUrl = resolvePhotoUrl(user)
                var photoData = ""
                try {
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    if (doc.exists()) {
                        username = doc.getString("username") ?: username
                        isAdmin = doc.getBoolean("isAdmin") ?: false
                        photoUrl = doc.getString("photoUrl")?.takeIf { it.isNotBlank() } ?: photoUrl
                        photoData = doc.getString("photoData") ?: ""
                    }
                } catch (_: Exception) {}

                Result.success(
                    AuthUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "",
                        username = username,
                        isAdmin = isAdmin,
                        linkedProviders = user.providerData.map { it.providerId },
                        photoUrl = photoUrl,
                        photoData = photoData
                    )
                )
            } else {
                Result.failure(Exception("Usuario no encontrado post-autenticación"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()

                val username = displayName.lowercase().replace(" ", "_")
                // Save to Firestore
                val userMap = mapOf(
                    "email" to email,
                    "displayName" to displayName,
                    "username" to username,
                    "isAdmin" to false,
                    "joinDate" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(userMap).await()

                Result.success(
                    AuthUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = displayName,
                        username = username,
                        isAdmin = false
                    )
                )
            } else {
                Result.failure(Exception("Error al crear la cuenta en Firebase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<AuthUser> {
        return Result.failure(Exception("Usa la actividad de Android para iniciar sesión con Google"))
    }

    override suspend fun linkWithGoogle(): Result<AuthUser> {
        return Result.failure(Exception("Usa la pantalla de Perfil de Android para vincular Google"))
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val user = firebaseAuth.currentUser
            ?: return Result.failure(Exception("No hay una sesión activa"))
        return try {
            val uid = user.uid
            try {
                firestore.collection("users").document(uid).collection("backups").document("latest").delete().await()
                firestore.collection("public_profiles").document(uid).delete().await()
                firestore.collection("users").document(uid).delete().await()
            } catch (_: Exception) {}
            user.delete().await()
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // --- CloudSyncManager implementation ---

    override suspend fun backupCatalog(userId: String, jsonString: String): Result<Unit> {
        return try {
            val catalog = Json { ignoreUnknownKeys = true }.decodeFromString<CloudCatalogBackupDto>(jsonString)
            val backupMap = hashMapOf(
                "songs" to catalog.songs.map { song ->
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
                "setlists" to catalog.setlists.map { setlistDetail ->
                    hashMapOf(
                        "setlist" to hashMapOf(
                            "id" to setlistDetail.setlist.id,
                            "name" to setlistDetail.setlist.name,
                            "description" to setlistDetail.setlist.description
                        ),
                        "songs" to setlistDetail.songs
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

    override suspend fun restoreCatalog(userId: String): Result<String> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("backups")
                .document("latest")
                .get()
                .await()

            if (snapshot.exists()) {
                val data = snapshot.data ?: return Result.failure(Exception("Datos vacíos en la nube."))
                @Suppress("UNCHECKED_CAST")
                val songsData = data["songs"] as? List<Map<String, Any>> ?: emptyList()
                val parsedSongs = songsData.map { map ->
                    CloudSongDto(
                        id = (map["id"] as? Long)?.toInt() ?: 0,
                        title = map["title"] as? String ?: "",
                        artist = map["artist"] as? String ?: "",
                        key = map["key"] as? String ?: "",
                        tuning = map["tuning"] as? String ?: "Estándar (E A D G B E)",
                        bpm = (map["bpm"] as? Long)?.toInt() ?: 120,
                        content = map["content"] as? String ?: "",
                        isDraft = map["isDraft"] as? Boolean ?: false
                    )
                }

                @Suppress("UNCHECKED_CAST")
                val setlistsData = data["setlists"] as? List<Map<String, Any>> ?: emptyList()
                val parsedSetlists = setlistsData.map { map ->
                    @Suppress("UNCHECKED_CAST")
                    val setlistMap = map["setlist"] as? Map<String, Any> ?: emptyMap()
                    @Suppress("UNCHECKED_CAST")
                    val songIds = (map["songs"] as? List<Long>)?.map { it.toInt() } ?: emptyList()
                    CloudSetlistDto(
                        setlist = CloudSetlistDetailDto(
                            id = (setlistMap["id"] as? Long)?.toInt() ?: 0,
                            name = setlistMap["name"] as? String ?: "",
                            description = setlistMap["description"] as? String ?: ""
                        ),
                        songs = songIds
                    )
                }

                val dto = CloudCatalogBackupDto(songs = parsedSongs, setlists = parsedSetlists)
                val jsonString = Json { prettyPrint = false; ignoreUnknownKeys = true }.encodeToString(dto)
                Result.success(jsonString)
            } else {
                Result.failure(Exception("No se encontró ningún respaldo en la nube."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncPublicProfile(
        uid: String,
        displayName: String? = null,
        username: String? = null,
        photoUrl: String? = null,
        photoData: String? = null
    ) {
        if (uid.isBlank()) return
        val payload = hashMapOf<String, Any>()
        displayName?.let { payload["displayName"] = it.take(100) }
        username?.let { payload["username"] = it.take(60) }
        photoUrl?.let { payload["photoUrl"] = it.take(2048) }
        photoData?.let { payload["photoData"] = it.take(180000) }
        if (payload.isEmpty()) return
        runCatching {
            firestore.collection("public_profiles").document(uid)
                .set(payload, SetOptions.merge()).await()
        }
    }

    override suspend fun updateUsername(userId: String, newUsername: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update("username", newUsername).await()
            syncPublicProfile(uid = userId, username = newUsername)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfilePhoto(userId: String, photoData: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update("photoData", photoData).await()
            syncPublicProfile(uid = userId, photoData = photoData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pickProfilePhoto(): Result<String?> {
        return Result.failure(Exception("Selector de galería no disponible en este módulo."))
    }

    override suspend fun fetchUsers(): Result<List<AdminUserModel>> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            val userList = snapshot.documents.map { doc ->
                val email = doc.getString("email") ?: ""
                val username = doc.getString("username") ?: (doc.getString("displayName") ?: "musico")
                val isAdmin = doc.getBoolean("isAdmin") ?: false
                val photoUrl = doc.getString("photoUrl") ?: ""
                val photoData = doc.getString("photoData") ?: ""
                AdminUserModel(
                    uid = doc.id,
                    username = username,
                    email = email,
                    role = if (isAdmin) "ADMIN" else "USER",
                    photoUrl = photoUrl,
                    photoData = photoData
                )
            }
            Result.success(userList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).delete().await()
            runCatching { firestore.collection("public_profiles").document(userId).delete().await() }
            val backups = firestore.collection("users").document(userId).collection("backups").get().await()
            for (doc in backups.documents) {
                doc.reference.delete().await()
            }
            val contribs = firestore.collection("contributions").whereEqualTo("authorId", userId).get().await()
            for (doc in contribs.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserRole(userId: String, isAdmin: Boolean): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update("isAdmin", isAdmin).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchContributions(): Result<List<CommunityPostModel>> {
        return try {
            val snapshot = firestore.collection("contributions").get().await()
            val baseList = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val approved = doc.getBoolean("isApproved") ?: doc.getBoolean("approved") ?: false
                CommunityPostModel(
                    id = doc.id,
                    title = title,
                    artist = doc.getString("artist") ?: "",
                    authorId = doc.getString("authorId") ?: "",
                    authorUsername = doc.getString("authorName") ?: "Músico",
                    bpm = (doc.getLong("bpm") ?: 120L).toInt(),
                    key = doc.getString("key") ?: "C",
                    tuning = doc.getString("tuning") ?: "E Standard",
                    lyricsAndChords = doc.getString("content") ?: "",
                    status = if (approved) "APPROVED" else "PENDING",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }.sortedByDescending { it.timestamp }

            val distinctAuthorIds = baseList.map { it.authorId.trim() }.filter { it.isNotBlank() }.distinct()
            val avatars = if (distinctAuthorIds.isNotEmpty()) {
                coroutineScope {
                    distinctAuthorIds.map { uid ->
                        async {
                            val pub = runCatching { firestore.collection("public_profiles").document(uid).get().await() }.getOrNull()
                            uid to ((pub?.getString("photoUrl") ?: "") to (pub?.getString("photoData") ?: ""))
                        }
                    }.awaitAll().toMap()
                }
            } else emptyMap()

            val enriched = baseList.map { post ->
                val pair = avatars[post.authorId]
                if (pair != null) post.copy(authorPhotoUrl = pair.first, authorPhotoData = pair.second) else post
            }
            Result.success(enriched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun publishContribution(song: SongItemModel, authorId: String, authorName: String): Result<Unit> {
        return try {
            val doc = firestore.collection("contributions").document()
            val payload = hashMapOf(
                "id" to doc.id,
                "title" to song.title.trim().ifBlank { "Sin título" }.take(200),
                "artist" to song.artist.trim().ifBlank { "Original" }.take(200),
                "key" to song.key.trim().ifBlank { "C" }.take(50),
                "tuning" to song.tuning.trim().ifBlank { "E Standard" }.take(100),
                "bpm" to song.bpm.coerceIn(0, 500),
                "content" to song.lyricsAndChords.trim().ifBlank { "Sin letra ni acordes" }.take(50000),
                "authorId" to authorId,
                "authorName" to authorName.trim().ifBlank { "Músico Riff Forge" }.take(100),
                "isApproved" to false,
                "approved" to false,
                "timestamp" to System.currentTimeMillis()
            )
            doc.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveContribution(id: String): Result<Unit> {
        return try {
            firestore.collection("contributions").document(id)
                .update(mapOf("isApproved" to true, "approved" to true))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
