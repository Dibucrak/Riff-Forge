package com.riffforge.feature_auth.data.repository

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.riffforge.R
import com.riffforge.feature_auth.domain.model.AuthUser
import com.riffforge.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: Exception("Error desconocido en Firebase"))
        }
    }
}

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private fun FirebaseUser.resolvePhotoUrl(): String? {
        val raw = providerData.firstOrNull { it.providerId == "google.com" && it.photoUrl != null }?.photoUrl
            ?: photoUrl
            ?: providerData.firstOrNull { it.photoUrl != null }?.photoUrl
        val str = raw?.toString()?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return str.replace("=s96-c", "=s400-c")
    }

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.let { firebaseUser ->
                AuthUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    linkedProviders = firebaseUser.providerData.map { it.providerId },
                    photoUrl = firebaseUser.resolvePhotoUrl()
                )
            }
            trySend(user)
        }

        firebaseAuth.addAuthStateListener(listener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).awaitTask()
            val user = authResult.user
            if (user != null) {
                Result.success(
                    AuthUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName,
                        linkedProviders = user.providerData.map { it.providerId },
                        photoUrl = user.resolvePhotoUrl()
                    )
                )
            } else {
                Result.failure(Exception("Usuario no encontrado post-autenticación"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).awaitTask()
            val user = authResult.user
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()

                val userMap = mapOf(
                    "email" to email,
                    "displayName" to displayName,
                    "username" to displayName.lowercase().replace(" ", "_"),
                    "isAdmin" to false,
                    "joinDate" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(userMap).await()

                Result.success(
                    AuthUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = displayName,
                        linkedProviders = user.providerData.map { it.providerId },
                        photoUrl = user.resolvePhotoUrl()
                    )
                )
            } else {
                Result.failure(Exception("Error al crear la cuenta en Firebase"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    private suspend fun requestGoogleIdToken(activity: Activity): String {
        val credentialManager = CredentialManager.create(activity)
        val webClientId = activity.getString(R.string.default_web_client_id)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        val response = credentialManager.getCredential(
            request = request,
            context = activity
        )
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
        return googleIdTokenCredential.idToken
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<AuthUser> {
        return try {
            val idToken = requestGoogleIdToken(activity)
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).awaitTask()

            val user = authResult.user
            if (user != null) {
                ensureFirestoreUserDocument(user)
                val resolvedPhoto = user.resolvePhotoUrl()
                if (!resolvedPhoto.isNullOrBlank()) {
                    runCatching {
                        firestore.collection("users").document(user.uid)
                            .update("photoUrl", resolvedPhoto).await()
                    }
                }
                Result.success(
                    AuthUser(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName,
                        linkedProviders = user.providerData.map { it.providerId },
                        photoUrl = resolvedPhoto
                    )
                )
            } else {
                Result.failure(Exception("No se pudo completar el inicio de sesión con Google"))
            }
        } catch (_: GetCredentialCancellationException) {
            Result.failure(Exception("Inicio de sesión con Google cancelado."))
        } catch (_: NoCredentialException) {
            Result.failure(Exception("No se encontraron cuentas de Google en este dispositivo."))
        } catch (e: GetCredentialException) {
            Result.failure(Exception(e.errorMessage?.toString() ?: e.localizedMessage ?: "Error con el selector de cuentas de Google"))
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    override suspend fun linkWithGoogle(activity: Activity): Result<AuthUser> {
        val current = firebaseAuth.currentUser
            ?: return Result.failure(Exception("Debes iniciar sesión antes de vincular tu cuenta"))
        return try {
            val idToken = requestGoogleIdToken(activity)
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = current.linkWithCredential(firebaseCredential).awaitTask()
            val updatedUser = authResult.user ?: current
            try { updatedUser.reload().awaitTask() } catch (_: Exception) {}
            val resolvedPhoto = updatedUser.resolvePhotoUrl()
            if (!resolvedPhoto.isNullOrBlank()) {
                runCatching {
                    firestore.collection("users").document(updatedUser.uid)
                        .update("photoUrl", resolvedPhoto).await()
                }
            }
            syncPublicProfile(
                uid = updatedUser.uid,
                displayName = updatedUser.displayName,
                photoUrl = resolvedPhoto
            )
            Result.success(
                AuthUser(
                    uid = updatedUser.uid,
                    email = updatedUser.email ?: "",
                    displayName = updatedUser.displayName,
                    linkedProviders = updatedUser.providerData.map { it.providerId },
                    photoUrl = resolvedPhoto
                )
            )
        } catch (_: GetCredentialCancellationException) {
            Result.failure(Exception("Vinculación con Google cancelada."))
        } catch (_: NoCredentialException) {
            Result.failure(Exception("No se encontraron cuentas de Google en este dispositivo."))
        } catch (e: GetCredentialException) {
            Result.failure(Exception(e.errorMessage?.toString() ?: e.localizedMessage ?: "Error con el selector de cuentas de Google"))
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(Exception("Por favor introduce un correo electrónico válido"))
        }
        return try {
            firebaseAuth.sendPasswordResetEmail(cleanEmail).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val user = firebaseAuth.currentUser
            ?: return Result.failure(Exception("No hay una sesión activa para eliminar"))

        val uid = user.uid
        var savedUserDoc: Map<String, Any>? = null
        var savedBackupDoc: Map<String, Any>? = null
        val savedContributions = mutableListOf<Pair<String, Map<String, Any>>>()

        return try {
            // Snapshot private docs & contributions so we can restore them if Firebase requires recent re-login
            val userDocRef = firestore.collection("users").document(uid)
            val backupDocRef = userDocRef.collection("backups").document("latest")

            try {
                val userSnap = userDocRef.get().await()
                if (userSnap.exists()) savedUserDoc = userSnap.data
                val backupSnap = backupDocRef.get().await()
                if (backupSnap.exists()) savedBackupDoc = backupSnap.data
                val contribSnap = firestore.collection("contributions")
                    .whereEqualTo("authorUid", uid)
                    .get()
                    .await()
                contribSnap.documents.forEach { doc ->
                    doc.data?.let { data -> savedContributions.add(doc.id to data) }
                }

                savedContributions.forEach { (docId, _) ->
                    firestore.collection("contributions").document(docId).delete().await()
                }
                if (savedBackupDoc != null) backupDocRef.delete().await()
                if (savedUserDoc != null) userDocRef.delete().await()
                runCatching { firestore.collection("public_profiles").document(uid).delete().await() }
            } catch (_: Exception) {
                // Proceed to Auth deletion even if Firestore docs did not exist
            }

            user.delete().awaitTask()
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            // Restore Firestore docs because Auth deletion was rejected due to stale session
            try {
                val userDocRef = firestore.collection("users").document(uid)
                savedUserDoc?.let { userDocRef.set(it).await() }
                savedBackupDoc?.let { userDocRef.collection("backups").document("latest").set(it).await() }
                savedContributions.forEach { (docId, data) ->
                    firestore.collection("contributions").document(docId).set(data).await()
                }
            } catch (_: Exception) {}
            Result.failure(
                Exception("Por seguridad, cierra sesión y vuelve a entrar antes de eliminar tu cuenta.")
            )
        } catch (e: Exception) {
            try {
                val userDocRef = firestore.collection("users").document(uid)
                savedUserDoc?.let { userDocRef.set(it).await() }
                savedBackupDoc?.let { userDocRef.collection("backups").document("latest").set(it).await() }
                savedContributions.forEach { (docId, data) ->
                    firestore.collection("contributions").document(docId).set(data).await()
                }
            } catch (_: Exception) {}
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
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

    private suspend fun ensureFirestoreUserDocument(user: FirebaseUser) {
        try {
            val docRef = firestore.collection("users").document(user.uid)
            val snapshot = docRef.get().await()
            val resolvedPhoto = user.resolvePhotoUrl()
            if (!snapshot.exists()) {
                val email = user.email.orEmpty()
                val displayName = user.displayName?.takeIf { it.isNotBlank() }
                    ?: email.substringBefore("@").ifBlank { "Guitarrista" }
                val username = displayName.lowercase()
                    .replace(Regex("[^a-z0-9_]"), "_")
                    .trim('_')
                    .ifBlank { "musico" }
                val userMap = mutableMapOf<String, Any>(
                    "email" to email,
                    "displayName" to displayName,
                    "username" to username,
                    "isAdmin" to false,
                    "joinDate" to System.currentTimeMillis()
                )
                resolvedPhoto?.let { userMap["photoUrl"] = it }
                docRef.set(userMap).await()
                syncPublicProfile(
                    uid = user.uid,
                    displayName = displayName,
                    username = username,
                    photoUrl = resolvedPhoto,
                    photoData = ""
                )
            } else {
                val existingUrl = snapshot.getString("photoUrl").orEmpty()
                val effectivePhotoUrl = existingUrl.ifBlank { resolvedPhoto.orEmpty() }
                syncPublicProfile(
                    uid = user.uid,
                    displayName = snapshot.getString("displayName") ?: user.displayName,
                    username = snapshot.getString("username"),
                    photoUrl = effectivePhotoUrl.takeIf { it.isNotBlank() },
                    photoData = snapshot.getString("photoData")
                )
            }
        } catch (_: Exception) {
            // Non-blocking: user is already authenticated in Firebase Auth
        }
    }

    private fun mapFirebaseError(e: Exception): String {
        val code = (e as? FirebaseAuthException)?.errorCode
        return when (code) {
            "ERROR_OPERATION_NOT_ALLOWED" ->
                "Proveedor desactivado en Firebase Console. Activa Google en Authentication → Sign-in method."
            "ERROR_INVALID_EMAIL" -> "El formato del correo electrónico no es válido."
            "ERROR_USER_NOT_FOUND" -> "No existe ninguna cuenta registrada con ese correo."
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Correo o contraseña incorrectos."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo electrónico ya está registrado."
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres."
            "ERROR_REQUIRES_RECENT_LOGIN" ->
                "Por seguridad, cierra sesión y vuelve a entrar antes de realizar esta acción."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                "Ya existe una cuenta con este correo usando otro método de inicio de sesión."
            "ERROR_PROVIDER_ALREADY_LINKED" ->
                "Tu cuenta ya está vinculada con este proveedor."
            "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                "Esa cuenta externa ya está vinculada a otro usuario de Riff Forge."
            else -> e.localizedMessage ?: "Error de autenticación en Firebase"
        }
    }
}
