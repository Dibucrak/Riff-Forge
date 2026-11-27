package com.riffforge.feature_cloud_backup.presentation

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.core.domain.model.CloudBackupData
import com.riffforge.core.domain.repository.CloudSyncRepository
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.feature_auth.domain.use_case.AuthUseCases
import com.riffforge.feature_setlists.domain.use_case.SetlistUseCases
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudBackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
        observeCatalogCounts()
    }

    fun onEvent(event: CloudBackupEvent) {
        when (event) {
            is CloudBackupEvent.BackupToCloud -> performBackup()
            is CloudBackupEvent.RestoreFromCloud -> performRestore()
        }
    }

    private fun observeCatalogCounts() {
        songUseCases.getSongs()
            .onEach { songs -> _state.value = state.value.copy(songCount = songs.size) }
            .launchIn(viewModelScope)
        setlistUseCases.getSetlists()
            .onEach { setlists -> _state.value = state.value.copy(setlistCount = setlists.size) }
            .launchIn(viewModelScope)
    }

    private fun getCurrentUser() {
        authUseCases.getCurrentUser()
            .onEach { user ->
                if (user != null) {
                    val savedSync = AppSettings.prefs(context)
                        .getString("last_cloud_backup_${user.uid}", "")
                        .orEmpty()
                    _state.value = state.value.copy(uid = user.uid, lastSyncLabel = savedSync)
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
                    val label = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    AppSettings.prefs(context)
                        .edit()
                        .putString("last_cloud_backup_$uid", label)
                        .apply()
                    _state.value = state.value.copy(lastSyncLabel = label)
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
                        detail.songs.forEach { song ->
                            setlistUseCases.addSongToSetlist(setId.toInt(), song.id)
                        }
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
