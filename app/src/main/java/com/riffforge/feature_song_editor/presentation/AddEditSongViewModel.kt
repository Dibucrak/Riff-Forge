package com.riffforge.feature_song_editor.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditSongViewModel @Inject constructor(
    private val songUseCases: SongUseCases
) : ViewModel() {

    private val _songTitle = mutableStateOf("")
    val songTitle: State<String> = _songTitle

    private val _songArtist = mutableStateOf("")
    val songArtist: State<String> = _songArtist

    private val _songKey = mutableStateOf("C")
    val songKey: State<String> = _songKey

    private val _songTuning = mutableStateOf("E Standard")
    val songTuning: State<String> = _songTuning

    private val _songBpm = mutableStateOf("120")
    val songBpm: State<String> = _songBpm

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SaveSong : UiEvent()
    }

    fun onEvent(event: AddEditSongEvent) {
        when (event) {
            is AddEditSongEvent.EnteredTitle -> _songTitle.value = event.value
            is AddEditSongEvent.EnteredArtist -> _songArtist.value = event.value
            is AddEditSongEvent.EnteredKey -> _songKey.value = event.value
            is AddEditSongEvent.EnteredTuning -> _songTuning.value = event.value
            is AddEditSongEvent.EnteredBpm -> _songBpm.value = event.value
            is AddEditSongEvent.SaveSong -> {
                viewModelScope.launch {
                    try {
                        val bpmInt = _songBpm.value.toIntOrNull() ?: 120
                        songUseCases.addSong(
                            Song(
                                title = _songTitle.value,
                                artist = _songArtist.value,
                                key = _songKey.value,
                                tuning = _songTuning.value,
                                bpm = bpmInt,
                                isDraft = false
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveSong)
                    } catch (e: Exception) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Error desconocido al guardar la canción"
                            )
                        )
                    }
                }
            }
        }
    }
}