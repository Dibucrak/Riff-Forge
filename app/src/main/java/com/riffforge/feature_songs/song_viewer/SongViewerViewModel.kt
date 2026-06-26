package com.riffforge.feature_songs.presentation.song_viewer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewerViewModel @Inject constructor(
    private val songUseCases: SongUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(SongViewerState())
    val state: State<SongViewerState> = _state

    init {
        savedStateHandle.get<Int>("songId")?.let { songId ->
            if (songId != -1) {
                loadSong(songId)
            }
        }
    }

    fun onEvent(event: SongViewerEvent) {
        when (event) {
            is SongViewerEvent.ToggleAutoScroll -> {
                _state.value = state.value.copy(
                    isAutoScrolling = !state.value.isAutoScrolling
                )
            }
            is SongViewerEvent.IncreaseSpeed -> {
                if (state.value.scrollSpeed < 10) {
                    _state.value = state.value.copy(scrollSpeed = state.value.scrollSpeed + 1)
                }
            }
            is SongViewerEvent.DecreaseSpeed -> {
                if (state.value.scrollSpeed > 1) {
                    _state.value = state.value.copy(scrollSpeed = state.value.scrollSpeed - 1)
                }
            }
            is SongViewerEvent.IncreaseTextSize -> {
                if (state.value.textSizeSp < 32f) {
                    _state.value = state.value.copy(textSizeSp = state.value.textSizeSp + 2f)
                }
            }
            is SongViewerEvent.DecreaseTextSize -> {
                if (state.value.textSizeSp > 10f) {
                    _state.value = state.value.copy(textSizeSp = state.value.textSizeSp - 2f)
                }
            }
        }
    }

    private fun loadSong(id: Int) {
        viewModelScope.launch {
            val fetchedSong = songUseCases.getSongById(id)
            _state.value = state.value.copy(
                song = fetchedSong,
                isLoading = false
            )
        }
    }
}