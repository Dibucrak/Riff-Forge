package com.riffforge.feature_profile.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.core.domain.model.CloudBackupData
import com.riffforge.core.domain.repository.CloudSyncRepository
import com.riffforge.feature_auth.domain.use_case.AuthUseCases
import com.riffforge.feature_notifications.domain.manager.ReminderManager
import com.riffforge.feature_setlists.domain.model.SetlistDetail
import com.riffforge.feature_setlists.domain.use_case.SetlistUseCases
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val authUseCases: AuthUseCases,
    private val cloudSyncRepository: CloudSyncRepository,
    private val songUseCases: SongUseCases,
    private val setlistUseCases: SetlistUseCases,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getUserJob: Job? = null

    var isReminderActive = mutableStateOf(false)

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
                    authUseCases.signOut()
                    _eventFlow.emit(UiEvent.SignOutSuccess)
                }
            }
            is ProfileEvent.BackupToCloud -> performBackup()
            is ProfileEvent.RestoreFromCloud -> performRestore()
            is ProfileEvent.ToggleReminder -> {
                isReminderActive.value = event.isEnabled
                if (event.isEnabled) {
                    reminderManager.scheduleDailyReminder()
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar("Recordatorio diario activado.")) }
                } else {
                    reminderManager.cancelReminder()
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowSnackbar("Recordatorios desactivados.")) }
                }
            }
        }
    }

    private fun getCurrentUser() {
        getUserJob?.cancel()
        getUserJob = authUseCases.getCurrentUser()
            .onEach { user ->
                if (user != null) {
                    _state.value = state.value.copy(
                        email = user.email,
                        displayName = user.displayName ?: "Guitarrista",
                        uid = user.uid,
                        isLoading = false
                    )
                } else {
                    _state.value = state.value.copy(isLoading = false)
                }
            }
            .launchIn(viewModelScope)
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
                    data.songs.forEach { songUseCases.addSong(it) }
                    data.setlists.forEach { detail ->
                        val setId = setlistUseCases.addSetlist(detail.setlist)
                        detail.songs.forEach { song -> setlistUseCases.addSongToSetlist(setId.toInt(), song.id) }
                    }
                    _eventFlow.emit(UiEvent.ShowSnackbar("Repertorio restaurado."))
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