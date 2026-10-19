package com.riffforge.feature_cloud_backup.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.core.domain.model.CloudBackupData
import com.riffforge.core.domain.repository.CloudSyncRepository
import com.riffforge.feature_auth.domain.use_case.AuthUseCases
import com.riffforge.feature_setlists.domain.use_case.SetlistUseCases
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudBackupViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val cloudSyncRepository: CloudSyncRepository,
    private val songUseCases: SongUseCases,
    private val setlistUseCases: SetlistUseCases
) : ViewModel() {

    private val _state = mutableStateOf(CloudBackupState())
    val state: State<CloudBackupState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        getCurrentUser()
    }

    fun onEvent(event: CloudBackupEvent) {
        when (event) {
            is CloudBackupEvent.BackupToCloud -> performBackup()
            is CloudBackupEvent.RestoreFromCloud -> performRestore()
        }
    }

    private fun getCurrentUser() {
        authUseCases.getCurrentUser()
            .onEach { user ->
                if (user != null) {
                    _state.value = state.value.copy(uid = user.uid)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun performBackup() {
        viewModelScope.launch {
            val uid = state.value.uid
            if (uid.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Debes iniciar sesión para respaldar."))
                return@launch
            }
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
            if (uid.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Debes iniciar sesión para restaurar."))
                return@launch
            }
            _state.value = state.value.copy(isSyncing = true)
            try {
                val result = cloudSyncRepository.restoreCatalog(uid)
                result.onSuccess { data ->
                    data.songs.forEach { songUseCases.addSong(it) }
                    data.setlists.forEach { detail ->
                        val setId = setlistUseCases.addSetlist(detail.setlist)
                        detail.songs.forEach { song ->
                            setlistUseCases.addSongToSetlist(setId.toInt(), song.id)
                        }
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
