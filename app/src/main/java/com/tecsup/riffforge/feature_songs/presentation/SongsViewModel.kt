package com.riffforge.feature_songs.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val songUseCases: SongUseCases
) : ViewModel() {

    private val _state = mutableStateOf(SongsState())
    val state: State<SongsState> = _state

    private var getSongsJob: Job? = null

    init {
        getSongs()
    }

    fun onEvent(event: SongsEvent) {
        when (event) {
            is SongsEvent.LoadSongs -> {
                getSongs()
            }
            is SongsEvent.DeleteSong -> {
            }
        }
    }

    private fun getSongs() {
        getSongsJob?.cancel()

        _state.value = state.value.copy(isLoading = true)

        getSongsJob = songUseCases.getSongs()
            .onEach { songs ->
                _state.value = state.value.copy(
                    songs = songs,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
}