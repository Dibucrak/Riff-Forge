package com.riffforge.feature_songs.presentation.song_viewer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_setlists.domain.use_case.SetlistUseCases
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import com.riffforge.feature_songs.domain.util.ChordTransposer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewerViewModel @Inject constructor(
    private val songUseCases: SongUseCases,
    private val setlistUseCases: SetlistUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(SongViewerState())
    val state: State<SongViewerState> = _state

    init {
        val songId = savedStateHandle.get<Int>("songId") ?: -1
        val setId = savedStateHandle.get<Int>("setId") ?: -1

        if (songId != -1) {
            loadSong(songId, if (setId != -1) setId else null)
        }
    }

    fun onEvent(event: SongViewerEvent) {
        when (event) {
            is SongViewerEvent.ToggleAutoScroll -> {
                _state.value = state.value.copy(isAutoScrolling = !state.value.isAutoScrolling)
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
            is SongViewerEvent.TransposeUp -> {
                if (state.value.transposeSemitones < 12) {
                    applyHarmonicShift(transposeDiff = 1, capoDiff = 0)
                }
            }
            is SongViewerEvent.TransposeDown -> {
                if (state.value.transposeSemitones > -12) {
                    applyHarmonicShift(transposeDiff = -1, capoDiff = 0)
                }
            }
            is SongViewerEvent.CapoUp -> {
                if (state.value.capo < 12) {
                    applyHarmonicShift(transposeDiff = 0, capoDiff = 1)
                }
            }
            is SongViewerEvent.CapoDown -> {
                if (state.value.capo > 0) {
                    applyHarmonicShift(transposeDiff = 0, capoDiff = -1)
                }
            }
            is SongViewerEvent.ToggleFlats -> {
                val newPreferFlats = !state.value.preferFlats
                _state.value = state.value.copy(preferFlats = newPreferFlats)
                applyHarmonicShift(0, 0)
            }
        }
    }

    private fun loadSong(id: Int, setId: Int?) {
        viewModelScope.launch {
            val fetchedSong = songUseCases.getSongById(id)
            if (fetchedSong != null) {

                var currentSetlistId: Int? = null
                var setlistName: String? = null
                var prevId: Int? = null
                var nextId: Int? = null
                var currentIndex = 0
                var totalSongs = 0

                if (setId != null) {
                    val setlistDetail = setlistUseCases.getSetlistById(setId)
                    if (setlistDetail != null) {
                        currentSetlistId = setId
                        setlistName = setlistDetail.setlist.name
                        totalSongs = setlistDetail.songs.size
                        currentIndex = setlistDetail.songs.indexOfFirst { it.id == id }

                        if (currentIndex > 0) {
                            prevId = setlistDetail.songs[currentIndex - 1].id
                        }
                        if (currentIndex != -1 && currentIndex < totalSongs - 1) {
                            nextId = setlistDetail.songs[currentIndex + 1].id
                        }
                    }
                }

                _state.value = state.value.copy(
                    song = fetchedSong,
                    originalContent = fetchedSong.content,
                    displayedContent = fetchedSong.content,
                    isLoading = false,
                    currentSetlistId = currentSetlistId,
                    setlistName = setlistName,
                    previousSongId = prevId,
                    nextSongId = nextId,
                    currentSongIndex = currentIndex,
                    totalSongsInSet = totalSongs
                )
            } else {
                _state.value = state.value.copy(isLoading = false)
            }
        }
    }

    private fun applyHarmonicShift(transposeDiff: Int, capoDiff: Int) {
        val newTranspose = state.value.transposeSemitones + transposeDiff
        val newCapo = state.value.capo + capoDiff
        val totalShift = newTranspose - newCapo

        val newContent = ChordTransposer.transposeContent(
            content = state.value.originalContent,
            semitones = totalShift,
            preferFlats = state.value.preferFlats
        )

        _state.value = state.value.copy(
            transposeSemitones = newTranspose,
            capo = newCapo,
            displayedContent = newContent
        )
    }
}