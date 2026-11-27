package com.riffforge.feature_auth

import com.riffforge.core.domain.store.AdminUserModel
import com.riffforge.core.domain.store.CommunityPostModel
import com.riffforge.core.domain.store.SongItemModel
import com.riffforge.core.domain.sync.CloudSyncManager
import com.riffforge.feature_auth.domain.AuthManager
import com.riffforge.feature_auth.domain.AuthUser
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

@Serializable
private data class PublishContributionBridgePayload(
    val title: String,
    val artist: String,
    val key: String,
    val tuning: String,
    val bpm: Int,
    val content: String,
    val authorId: String,
    val authorName: String
)

/**
 * iOS implementation of AuthManager and CloudSyncManager using Firebase iOS SDK via Kotlin/Native interop.
 */
class IOSAuthManager : AuthManager, CloudSyncManager {

    private var currentAuthUser: AuthUser? = null
    private var activeUpdateUsernameNewName: String? = null
    private var activeUpdatePhotoData: String? = null

    private val _authStateFlow = MutableSharedFlow<AuthUser?>(replay = 1)
    override val currentUser: Flow<AuthUser?> = _authStateFlow

    // Pending auth requests that Swift (AuthBridge) will execute
    var pendingSignInEmail: String? = null
    var pendingSignInPassword: String? = null
    var pendingSignUpEmail: String? = null
    var pendingSignUpPassword: String? = null
    var pendingSignUpDisplayName: String? = null
    var pendingGoogleSignIn: Boolean = false
    var pendingLinkGoogle: Boolean = false
    var pendingResetEmail: String? = null
    var pendingDeleteAccount: Boolean = false
    var pendingSignOut: Boolean = false

    // Pending cloud sync / community / admin requests that Swift (AuthBridge) will execute
    var pendingBackupUserId: String? = null
    var pendingBackupJson: String? = null
    var pendingRestoreUserId: String? = null
    var pendingUpdateUsernameUserId: String? = null
    var pendingUpdateUsernameNewName: String? = null
    var pendingUpdatePhotoUserId: String? = null
    var pendingUpdatePhotoData: String? = null
    var pendingPickProfilePhoto: Boolean = false
    var pendingFetchUsers: Boolean = false
    var pendingUpdateRoleUserId: String? = null
    var pendingUpdateRoleIsAdmin: Boolean = false
    var pendingDeleteUserId: String? = null
    var pendingFetchContributions: Boolean = false
    var pendingPublishContributionJson: String? = null
    var pendingApproveContributionId: String? = null
    var pendingDeleteContributionId: String? = null

    // Stored continuations that Swift resumes via handle*Result
    private var signInContinuation: CancellableContinuation<Result<AuthUser>>? = null
    private var signUpContinuation: CancellableContinuation<Result<AuthUser>>? = null
    private var googleSignInContinuation: CancellableContinuation<Result<AuthUser>>? = null
    private var linkGoogleContinuation: CancellableContinuation<Result<AuthUser>>? = null
    private var passwordResetContinuation: CancellableContinuation<Result<Unit>>? = null
    private var deleteAccountContinuation: CancellableContinuation<Result<Unit>>? = null
    private var backupContinuation: CancellableContinuation<Result<Unit>>? = null
    private var restoreContinuation: CancellableContinuation<Result<String>>? = null
    private var updateUsernameContinuation: CancellableContinuation<Result<Unit>>? = null
    private var updatePhotoContinuation: CancellableContinuation<Result<Unit>>? = null
    private var pickProfilePhotoContinuation: CancellableContinuation<Result<String?>>? = null
    private var fetchUsersContinuation: CancellableContinuation<Result<List<AdminUserModel>>>? = null
    private var updateRoleContinuation: CancellableContinuation<Result<Unit>>? = null
    private var deleteUserContinuation: CancellableContinuation<Result<Unit>>? = null
    private var fetchContributionsContinuation: CancellableContinuation<Result<List<CommunityPostModel>>>? = null
    private var publishContributionContinuation: CancellableContinuation<Result<Unit>>? = null
    private var approveContributionContinuation: CancellableContinuation<Result<Unit>>? = null
    private var deleteContributionContinuation: CancellableContinuation<Result<Unit>>? = null

    private fun parseProviders(csv: String): List<String> =
        csv.split(",").map { it.trim() }.filter { it.isNotBlank() }

    private inline fun <T> safeResume(cont: CancellableContinuation<Result<T>>?, value: Result<T>) {
        if (cont != null && cont.isActive) {
            cont.resume(value)
        }
    }

    // --- Methods called directly from Swift (AuthBridge) ---

    fun onAuthStateChanged(
        uid: String,
        email: String,
        displayName: String,
        username: String,
        isAdmin: Boolean = false,
        linkedProvidersCsv: String = "",
        photoUrl: String = "",
        photoData: String = ""
    ) {
        val user = if (uid.isNotBlank()) {
            AuthUser(
                uid = uid,
                email = email,
                displayName = displayName,
                username = username,
                isAdmin = isAdmin,
                linkedProviders = parseProviders(linkedProvidersCsv),
                photoUrl = photoUrl,
                photoData = photoData
            )
        } else {
            null
        }
        currentAuthUser = user
        CoroutineScope(Dispatchers.Default).launch {
            _authStateFlow.emit(user)
        }
    }

    fun handleSignInResult(
        uid: String,
        email: String,
        displayName: String,
        username: String,
        isAdmin: Boolean = false,
        linkedProvidersCsv: String = "",
        photoUrl: String = "",
        photoData: String = "",
        error: String?
    ) {
        val cont = signInContinuation
        signInContinuation = null
        completeAuthContinuation(cont, uid, email, displayName, username, isAdmin, linkedProvidersCsv, photoUrl, photoData, error, "Inicio de sesión fallido")
    }

    fun handleSignUpResult(
        uid: String,
        email: String,
        displayName: String,
        username: String,
        isAdmin: Boolean = false,
        linkedProvidersCsv: String = "",
        photoUrl: String = "",
        photoData: String = "",
        error: String?
    ) {
        val cont = signUpContinuation
        signUpContinuation = null
        completeAuthContinuation(cont, uid, email, displayName, username, isAdmin, linkedProvidersCsv, photoUrl, photoData, error, "Registro fallido")
    }

    fun handleGoogleSignInResult(
        uid: String,
        email: String,
        displayName: String,
        username: String,
        isAdmin: Boolean = false,
        linkedProvidersCsv: String = "",
        photoUrl: String = "",
        photoData: String = "",
        error: String?
    ) {
        val cont = googleSignInContinuation
        googleSignInContinuation = null
        completeAuthContinuation(cont, uid, email, displayName, username, isAdmin, linkedProvidersCsv, photoUrl, photoData, error, "No se pudo iniciar sesión con Google")
    }

    fun handleLinkGoogleResult(
        uid: String,
        email: String,
        displayName: String,
        username: String,
        isAdmin: Boolean = false,
        linkedProvidersCsv: String = "",
        photoUrl: String = "",
        photoData: String = "",
        error: String?
    ) {
        val cont = linkGoogleContinuation
        linkGoogleContinuation = null
        completeAuthContinuation(cont, uid, email, displayName, username, isAdmin, linkedProvidersCsv, photoUrl, photoData, error, "No se pudo vincular con Google")
    }

    fun handlePasswordResetResult(success: Boolean, error: String?) {
        val cont = passwordResetContinuation
        passwordResetContinuation = null
        if (success) {
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "No se pudo enviar el enlace de recuperación")))
        }
    }

    fun handleDeleteAccountResult(success: Boolean, error: String?) {
        val cont = deleteAccountContinuation
        deleteAccountContinuation = null
        if (success) {
            currentAuthUser = null
            CoroutineScope(Dispatchers.Default).launch {
                _authStateFlow.emit(null)
            }
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "No se pudo eliminar la cuenta")))
        }
    }

    private fun completeAuthContinuation(
        cont: CancellableContinuation<Result<AuthUser>>?,
        uid: String,
        email: String,
        displayName: String,
        username: String,
        isAdmin: Boolean,
        linkedProvidersCsv: String,
        photoUrl: String,
        photoData: String,
        error: String?,
        fallbackError: String
    ) {
        if (error != null && error.isNotBlank()) {
            safeResume(cont, Result.failure(Exception(error)))
        } else if (uid.isNotBlank()) {
            val user = AuthUser(
                uid = uid,
                email = email,
                displayName = displayName,
                username = username,
                isAdmin = isAdmin,
                linkedProviders = parseProviders(linkedProvidersCsv),
                photoUrl = photoUrl,
                photoData = photoData
            )
            currentAuthUser = user
            CoroutineScope(Dispatchers.Default).launch {
                _authStateFlow.emit(user)
            }
            safeResume(cont, Result.success(user))
        } else {
            safeResume(cont, Result.failure(Exception(fallbackError)))
        }
    }

    fun handleSignOut() {
        currentAuthUser = null
        CoroutineScope(Dispatchers.Default).launch {
            _authStateFlow.emit(null)
        }
    }

    fun handleBackupResult(success: Boolean, error: String?) {
        val cont = backupContinuation
        backupContinuation = null
        if (success) {
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "Error al respaldar catálogo en la nube")))
        }
    }

    fun handleRestoreResult(json: String?, error: String?) {
        val cont = restoreContinuation
        restoreContinuation = null
        if (json != null && json.isNotBlank()) {
            safeResume(cont, Result.success(json))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "No se encontró ningún respaldo en la nube")))
        }
    }

    fun handleUpdateUsernameResult(success: Boolean, error: String?) {
        val cont = updateUsernameContinuation
        updateUsernameContinuation = null
        val appliedName = activeUpdateUsernameNewName
        activeUpdateUsernameNewName = null
        if (success) {
            val user = currentAuthUser
            if (user != null && !appliedName.isNullOrBlank()) {
                val updated = user.copy(username = appliedName)
                currentAuthUser = updated
                CoroutineScope(Dispatchers.Default).launch {
                    _authStateFlow.emit(updated)
                }
            }
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "Error al actualizar nombre de usuario")))
        }
    }

    fun handleUpdatePhotoResult(success: Boolean, error: String?) {
        val cont = updatePhotoContinuation
        updatePhotoContinuation = null
        val appliedPhoto = activeUpdatePhotoData
        activeUpdatePhotoData = null
        if (success) {
            val user = currentAuthUser
            if (user != null && appliedPhoto != null) {
                val updated = user.copy(photoData = appliedPhoto)
                currentAuthUser = updated
                CoroutineScope(Dispatchers.Default).launch {
                    _authStateFlow.emit(updated)
                }
            }
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "Error al actualizar foto de perfil")))
        }
    }

    fun handlePickProfilePhotoResult(base64: String?, error: String?) {
        val cont = pickProfilePhotoContinuation
        pickProfilePhotoContinuation = null
        if (error != null && error.isNotBlank()) {
            safeResume(cont, Result.failure(Exception(error)))
        } else {
            safeResume(cont, Result.success(base64))
        }
    }

    fun handleFetchUsersResult(usersJson: String?, error: String?) {
        val cont = fetchUsersContinuation
        fetchUsersContinuation = null
        if (error != null && error.isNotBlank()) {
            safeResume(cont, Result.failure(Exception(error)))
        } else if (usersJson != null) {
            try {
                val users = Json { ignoreUnknownKeys = true }.decodeFromString<List<AdminUserModel>>(usersJson)
                safeResume(cont, Result.success(users))
            } catch (e: Exception) {
                safeResume(cont, Result.failure(e))
            }
        } else {
            safeResume(cont, Result.success(emptyList()))
        }
    }

    fun handleUpdateUserRoleResult(success: Boolean, error: String?) {
        val cont = updateRoleContinuation
        updateRoleContinuation = null
        if (success) {
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "Error al actualizar rol de administrador")))
        }
    }

    fun handleDeleteUserResult(success: Boolean, error: String?) {
        val cont = deleteUserContinuation
        deleteUserContinuation = null
        if (success) {
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "Error al eliminar usuario")))
        }
    }

    fun handleFetchContributionsResult(contributionsJson: String?, error: String?) {
        val cont = fetchContributionsContinuation
        fetchContributionsContinuation = null
        if (error != null && error.isNotBlank()) {
            safeResume(cont, Result.failure(Exception(error)))
        } else if (contributionsJson != null) {
            try {
                val items = Json { ignoreUnknownKeys = true }.decodeFromString<List<CommunityPostModel>>(contributionsJson)
                safeResume(cont, Result.success(items))
            } catch (e: Exception) {
                safeResume(cont, Result.failure(e))
            }
        } else {
            safeResume(cont, Result.success(emptyList()))
        }
    }

    fun handlePublishContributionResult(success: Boolean, error: String?) {
        val cont = publishContributionContinuation
        publishContributionContinuation = null
        if (success) {
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "Error al enviar el aporte a Firestore")))
        }
    }

    fun handleApproveContributionResult(success: Boolean, error: String?) {
        val cont = approveContributionContinuation
        approveContributionContinuation = null
        if (success) {
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "Error al aprobar el aporte")))
        }
    }

    fun handleDeleteContributionResult(success: Boolean, error: String?) {
        val cont = deleteContributionContinuation
        deleteContributionContinuation = null
        if (success) {
            safeResume(cont, Result.success(Unit))
        } else {
            safeResume(cont, Result.failure(Exception(error ?: "Error al eliminar el aporte")))
        }
    }

    // --- AuthManager interface implementation ---

    override suspend fun signIn(email: String, password: String): Result<AuthUser> =
        suspendCancellableCoroutine { cont ->
            signInContinuation = cont
            cont.invokeOnCancellation { signInContinuation = null }
            pendingSignInEmail = email
            pendingSignInPassword = password
        }

    override suspend fun signUp(email: String, password: String, displayName: String): Result<AuthUser> =
        suspendCancellableCoroutine { cont ->
            signUpContinuation = cont
            cont.invokeOnCancellation { signUpContinuation = null }
            pendingSignUpEmail = email
            pendingSignUpPassword = password
            pendingSignUpDisplayName = displayName
        }

    override suspend fun signInWithGoogle(): Result<AuthUser> =
        suspendCancellableCoroutine { cont ->
            googleSignInContinuation = cont
            cont.invokeOnCancellation { googleSignInContinuation = null }
            pendingGoogleSignIn = true
        }

    override suspend fun linkWithGoogle(): Result<AuthUser> =
        suspendCancellableCoroutine { cont ->
            linkGoogleContinuation = cont
            cont.invokeOnCancellation { linkGoogleContinuation = null }
            pendingLinkGoogle = true
        }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            passwordResetContinuation = cont
            cont.invokeOnCancellation { passwordResetContinuation = null }
            pendingResetEmail = email
        }

    override suspend fun deleteAccount(): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            deleteAccountContinuation = cont
            cont.invokeOnCancellation { deleteAccountContinuation = null }
            pendingDeleteAccount = true
        }

    override suspend fun signOut() {
        pendingSignOut = true
        handleSignOut()
    }

    override fun isAuthenticated(): Boolean {
        return currentAuthUser != null
    }

    // --- CloudSyncManager interface implementation ---

    override suspend fun backupCatalog(userId: String, jsonString: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            backupContinuation = cont
            cont.invokeOnCancellation { backupContinuation = null }
            pendingBackupUserId = userId
            pendingBackupJson = jsonString
        }

    override suspend fun restoreCatalog(userId: String): Result<String> =
        suspendCancellableCoroutine { cont ->
            restoreContinuation = cont
            cont.invokeOnCancellation { restoreContinuation = null }
            pendingRestoreUserId = userId
        }

    override suspend fun updateUsername(userId: String, newUsername: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            activeUpdateUsernameNewName = newUsername
            updateUsernameContinuation = cont
            cont.invokeOnCancellation {
                updateUsernameContinuation = null
                activeUpdateUsernameNewName = null
            }
            pendingUpdateUsernameUserId = userId
            pendingUpdateUsernameNewName = newUsername
        }

    override suspend fun updateProfilePhoto(userId: String, photoData: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            activeUpdatePhotoData = photoData
            updatePhotoContinuation = cont
            cont.invokeOnCancellation {
                updatePhotoContinuation = null
                activeUpdatePhotoData = null
            }
            pendingUpdatePhotoUserId = userId
            pendingUpdatePhotoData = photoData
        }

    override suspend fun pickProfilePhoto(): Result<String?> =
        suspendCancellableCoroutine { cont ->
            pickProfilePhotoContinuation = cont
            cont.invokeOnCancellation { pickProfilePhotoContinuation = null }
            pendingPickProfilePhoto = true
        }

    override suspend fun fetchUsers(): Result<List<AdminUserModel>> =
        suspendCancellableCoroutine { cont ->
            fetchUsersContinuation = cont
            cont.invokeOnCancellation { fetchUsersContinuation = null }
            pendingFetchUsers = true
        }

    override suspend fun updateUserRole(userId: String, isAdmin: Boolean): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            updateRoleContinuation = cont
            cont.invokeOnCancellation { updateRoleContinuation = null }
            pendingUpdateRoleUserId = userId
            pendingUpdateRoleIsAdmin = isAdmin
        }

    override suspend fun deleteUser(userId: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            deleteUserContinuation = cont
            cont.invokeOnCancellation { deleteUserContinuation = null }
            pendingDeleteUserId = userId
        }

    override suspend fun fetchContributions(): Result<List<CommunityPostModel>> =
        suspendCancellableCoroutine { cont ->
            fetchContributionsContinuation = cont
            cont.invokeOnCancellation { fetchContributionsContinuation = null }
            pendingFetchContributions = true
        }

    override suspend fun publishContribution(song: SongItemModel, authorId: String, authorName: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            val payload = PublishContributionBridgePayload(
                title = song.title.trim().ifBlank { "Sin título" }.take(200),
                artist = song.artist.trim().ifBlank { "Original" }.take(200),
                key = song.key.trim().ifBlank { "C" }.take(50),
                tuning = song.tuning.trim().ifBlank { "E Standard" }.take(100),
                bpm = song.bpm.coerceIn(0, 500),
                content = song.lyricsAndChords.trim().ifBlank { "Sin letra ni acordes" }.take(50000),
                authorId = authorId,
                authorName = authorName.trim().ifBlank { "Músico Riff Forge" }.take(100)
            )
            publishContributionContinuation = cont
            cont.invokeOnCancellation { publishContributionContinuation = null }
            pendingPublishContributionJson = Json.encodeToString(payload)
        }

    override suspend fun approveContribution(id: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            approveContributionContinuation = cont
            cont.invokeOnCancellation { approveContributionContinuation = null }
            pendingApproveContributionId = id
        }

    override suspend fun deleteContribution(id: String): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            deleteContributionContinuation = cont
            cont.invokeOnCancellation { deleteContributionContinuation = null }
            pendingDeleteContributionId = id
        }
}
