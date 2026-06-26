package com.riffforge.feature_setlists.presentation.setlist_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_setlists.domain.use_case.SetlistUseCases
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetlistDetailViewModel @Inject constructor(
    private val setlistUseCases: SetlistUseCases,
    private val songUseCases: SongUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(SetlistDetailState())
    val state: State<SetlistDetailState> = _state

    private var getSetlistJob: Job? = null
    private var getSongsJob: Job? = null

    private var currentSetId: Int = -1

    init {
        savedStateHandle.get<Int>("setId")?.let { setId ->
            if (setId != -1) {
                currentSetId = setId
                getSetlistDetail(setId)
                getAllSongs()
            }
        }
    }

    fun onEvent(event: SetlistDetailEvent) {
        when (event) {
            is SetlistDetailEvent.ShowAddSongDialog -> {
                _state.value = state.value.copy(isAddingSong = true)
            }
            is SetlistDetailEvent.HideAddSongDialog -> {
                _state.value = state.value.copy(isAddingSong = false)
            }
            is SetlistDetailEvent.AddSongToSetlist -> {
                viewModelScope.launch {
                    if (currentSetId != -1) {
                        setlistUseCases.addSongToSetlist(currentSetId, event.songId)
                    }
                }
            }
            is SetlistDetailEvent.RemoveSongFromSetlist -> {
                // TODO: Implementar en el caso de uso futuro si se desea remover canciones
            }
        }
    }

    private fun getSetlistDetail(setId: Int) {
        getSetlistJob?.cancel()
        getSetlistJob = setlistUseCases.getSetlists()
            .onEach { setlists ->
                val detail = setlists.find { it.setlist.id == setId }
                _state.value = state.value.copy(
                    setlistDetail = detail,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    private fun getAllSongs() {
        getSongsJob?.cancel()
        getSongsJob = songUseCases.getSongs()
            .onEach { songs ->
                _state.value = state.value.copy(allSongs = songs)
            }
            .launchIn(viewModelScope)
    }
}