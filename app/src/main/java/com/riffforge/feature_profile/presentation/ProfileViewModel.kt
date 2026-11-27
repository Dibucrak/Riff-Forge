package com.riffforge.feature_profile.presentation

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.riffforge.core.domain.model.CloudBackupData
import com.riffforge.core.domain.repository.CloudSyncRepository
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.feature_auth.domain.use_case.AuthUseCases
import com.riffforge.feature_notifications.domain.manager.ReminderManager
import com.riffforge.feature_setlists.domain.repository.SetlistRepository
import com.riffforge.feature_setlists.domain.use_case.SetlistUseCases
import com.riffforge.feature_songs.domain.repository.SongRepository
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authUseCases: AuthUseCases,
    private val cloudSyncRepository: CloudSyncRepository,
    private val songUseCases: SongUseCases,
    private val setlistUseCases: SetlistUseCases,
    private val songRepository: SongRepository,
    private val setlistRepository: SetlistRepository,
    private val reminderManager: ReminderManager,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getUserJob: Job? = null

    var isReminderActive = mutableStateOf(
        AppSettings.prefs(context).getBoolean("practice_reminder_enabled", false)
    )

    sealed class UiEvent {
        object SignOutSuccess : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        getCurrentUser()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.SignOut -> {
                viewModelScope.launch {
                    runCatching {
                        setlistRepository.clearAllSetlists()
                        songRepository.clearAllSongs()
                    }
                    authUseCases.signOut()
                    _eventFlow.emit(UiEvent.SignOutSuccess)
                }
            }
            is ProfileEvent.DeleteAccount -> deleteAccount()
            is ProfileEvent.LinkWithGoogle -> linkWithGoogle(event.activity)
            is ProfileEvent.BackupToCloud -> performBackup()
            is ProfileEvent.RestoreFromCloud -> performRestore()
            is ProfileEvent.ToggleReminder -> {
                isReminderActive.value = event.isEnabled
                AppSettings.prefs(context)
                    .edit()
                    .putBoolean("practice_reminder_enabled", event.isEnabled)
                    .apply()
                if (event.isEnabled) {
                    reminderManager.scheduleDailyReminder()
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar("Recordatorio diario activado (19:00).")) }
                } else {
                    reminderManager.cancelReminder()
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar("Recordatorios desactivados.")) }
                }
            }
            is ProfileEvent.UpdateUsername -> updateUsername(event.newUsername)
            is ProfileEvent.UpdateProfilePhoto -> updateProfilePhoto(event.photoBase64)
            is ProfileEvent.ResetProfilePhoto -> resetProfilePhoto()
        }
    }

    private fun getCurrentUser() {
        getUserJob?.cancel()
        getUserJob = authUseCases.getCurrentUser()
            .onEach { user ->
                if (user != null) {
                    viewModelScope.launch {
                        try {
                            val userDoc = firestore.collection("users").document(user.uid).get().await()
                            val username = userDoc.getString("username") ?: user.displayName?.lowercase()?.replace(" ", "_") ?: ""
                            val isAdmin = userDoc.getBoolean("isAdmin") ?: false
                            val firestorePhotoUrl = userDoc.getString("photoUrl").orEmpty()
                            val resolvedPhotoUrl = user.photoUrl.orEmpty().ifBlank { firestorePhotoUrl }
                            val photoData = userDoc.getString("photoData").orEmpty()
                            if (!user.photoUrl.isNullOrBlank() && user.photoUrl != firestorePhotoUrl) {
                                runCatching {
                                    firestore.collection("users").document(user.uid)
                                        .update("photoUrl", user.photoUrl).await()
                                }
                            }
                            val displayName = user.displayName ?: "Guitarrista"
                            syncPublicProfile(
                                uid = user.uid,
                                displayName = displayName,
                                username = username,
                                photoUrl = resolvedPhotoUrl,
                                photoData = photoData
                            )
                            _state.value = state.value.copy(
                                email = user.email,
                                displayName = displayName,
                                username = username,
                                uid = user.uid,
                                isAdmin = isAdmin,
                                linkedProviders = user.linkedProviders,
                                photoUrl = resolvedPhotoUrl,
                                photoData = photoData,
                                isLoading = false
                            )
                        } catch (e: Exception) {
                            _state.value = state.value.copy(
                                email = user.email,
                                displayName = user.displayName ?: "Guitarrista",
                                username = user.displayName?.lowercase()?.replace(" ", "_") ?: "",
                                uid = user.uid,
                                isAdmin = false,
                                linkedProviders = user.linkedProviders,
                                photoUrl = user.photoUrl.orEmpty(),
                                isLoading = false
                            )
                        }
                    }
                } else {
                    _state.value = state.value.copy(isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            val uid = state.value.uid
            val cleaned = newUsername.trim().removePrefix("@").lowercase()
            if (uid.isBlank()) return@launch
            if (!cleaned.matches(Regex("^[a-z0-9_]{3,24}$"))) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Usa 3–24 caracteres (letras, números o _)."))
                return@launch
            }
            try {
                firestore.collection("users").document(uid).update("username", cleaned).await()
                syncPublicProfile(uid = uid, username = cleaned)
                _state.value = state.value.copy(username = cleaned)
                _eventFlow.emit(UiEvent.ShowSnackbar("Nombre de usuario actualizado."))
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error al actualizar: ${e.message}"))
            }
        }
    }

    private fun updateProfilePhoto(photoBase64: String) {
        viewModelScope.launch {
            val uid = state.value.uid
            if (uid.isBlank() || photoBase64.isBlank()) return@launch
            _state.value = state.value.copy(isUpdatingPhoto = true)
            try {
                firestore.collection("users").document(uid).update("photoData", photoBase64).await()
                syncPublicProfile(uid = uid, photoData = photoBase64)
                _state.value = state.value.copy(photoData = photoBase64, isUpdatingPhoto = false)
                _eventFlow.emit(UiEvent.ShowSnackbar("Foto de perfil actualizada."))
            } catch (e: Exception) {
                _state.value = state.value.copy(isUpdatingPhoto = false)
                _eventFlow.emit(UiEvent.ShowSnackbar("No se pudo guardar la foto: ${e.message}"))
            }
        }
    }

    private fun resetProfilePhoto() {
        viewModelScope.launch {
            val uid = state.value.uid
            if (uid.isBlank()) return@launch
            _state.value = state.value.copy(isUpdatingPhoto = true)
            try {
                firestore.collection("users").document(uid).update("photoData", "").await()
                syncPublicProfile(uid = uid, photoData = "")
                _state.value = state.value.copy(photoData = "", isUpdatingPhoto = false)
                val msg = if (state.value.photoUrl.isNotBlank()) {
                    "Foto de perfil de Google restaurada."
                } else {
                    "Foto personalizada eliminada."
                }
                _eventFlow.emit(UiEvent.ShowSnackbar(msg))
            } catch (e: Exception) {
                _state.value = state.value.copy(isUpdatingPhoto = false)
                _eventFlow.emit(UiEvent.ShowSnackbar("Error al restablecer la foto: ${e.message}"))
            }
        }
    }

    private fun linkWithGoogle(activity: android.app.Activity) {
        viewModelScope.launch {
            _state.value = state.value.copy(isLinking = true)
            val result = authUseCases.linkWithGoogle(activity)
            _state.value = state.value.copy(isLinking = false)
            result.onSuccess { updatedUser ->
                val resolvedUrl = updatedUser.photoUrl.orEmpty().ifBlank { state.value.photoUrl }
                syncPublicProfile(
                    uid = updatedUser.uid,
                    displayName = updatedUser.displayName ?: state.value.displayName,
                    photoUrl = resolvedUrl
                )
                _state.value = state.value.copy(
                    linkedProviders = updatedUser.linkedProviders,
                    photoUrl = resolvedUrl
                )
                _eventFlow.emit(UiEvent.ShowSnackbar("Cuenta vinculada con Google exitosamente."))
            }.onFailure { err ->
                _eventFlow.emit(UiEvent.ShowSnackbar(err.message ?: "No se pudo vincular con Google"))
            }
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

    private fun deleteAccount() {
        viewModelScope.launch {
            _state.value = state.value.copy(isDeletingAccount = true)
            val result = authUseCases.deleteAccount()
            _state.value = state.value.copy(isDeletingAccount = false)
            result.onSuccess {
                runCatching {
                    setlistRepository.clearAllSetlists()
                    songRepository.clearAllSongs()
                }
                _eventFlow.emit(UiEvent.SignOutSuccess)
            }.onFailure { err ->
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(err.message ?: "No se pudo eliminar la cuenta")
                )
            }
        }
    }

    private fun performBackup() {
        viewModelScope.launch {
            val uid = state.value.uid
            if (uid.isBlank()) return@launch
            _state.value = state.value.copy(isSyncing = true)
            try {
                val currentSongs = songUseCases.getSongs().first()
                val currentSetlists = setlistUseCases.getSetlists().first()
                val backupData = CloudBackupData(songs = currentSongs, setlists = currentSetlists)
                val result = cloudSyncRepository.backupCatalog(uid, backupData)
                result.onSuccess {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Repertorio respaldado exitosamente."))
                }.onFailure {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error al respaldar: ${it.message}"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error inesperado."))
            } finally {
                _state.value = state.value.copy(isSyncing = false)
            }
        }
    }

    private fun performRestore() {
        viewModelScope.launch {
            val uid = state.value.uid
            if (uid.isBlank()) return@launch
            _state.value = state.value.copy(isSyncing = true)
            try {
                val result = cloudSyncRepository.restoreCatalog(uid)
                result.onSuccess { data ->
                    data.songs.forEach { song ->
                        songUseCases.addSong(
                            song.copy(
                                title = song.title.ifBlank { "Sin título" },
                                artist = song.artist.ifBlank { "Artista Desconocido" }
                            )
                        )
                    }
                    data.setlists.forEach { detail ->
                        val setId = setlistUseCases.addSetlist(
                            detail.setlist.copy(
                                name = detail.setlist.name.ifBlank { "Setlist Restaurado" }
                            )
                        )
                        detail.songs.forEach { song -> setlistUseCases.addSongToSetlist(setId.toInt(), song.id) }
                    }
                    _eventFlow.emit(UiEvent.ShowSnackbar("Repertorio restaurado (${data.songs.size} canciones, ${data.setlists.size} setlists)."))
                }.onFailure {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error al restaurar: ${it.message}"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error en sincronización."))
            } finally {
                _state.value = state.value.copy(isSyncing = false)
            }
        }
    }
}