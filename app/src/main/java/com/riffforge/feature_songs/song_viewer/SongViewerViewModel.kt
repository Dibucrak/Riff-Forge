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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private var observeSongJob: Job? = null

    init {
        val songId = savedStateHandle.get<Int>("songId")
            ?: savedStateHandle.get<String>("songId")?.toIntOrNull()
            ?: -1
        val setId = savedStateHandle.get<Int>("setId")
            ?: savedStateHandle.get<String>("setId")?.toIntOrNull()
            ?: -1

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
                val steps = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 2.5f, 3.0f)
                val idx = steps.indexOfFirst { it >= state.value.scrollSpeed }.coerceAtLeast(0)
                if (idx < steps.lastIndex) {
                    _state.value = state.value.copy(scrollSpeed = steps[idx + 1])
                }
            }
            is SongViewerEvent.DecreaseSpeed -> {
                val steps = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 2.5f, 3.0f)
                val idx = steps.indexOfFirst { it >= state.value.scrollSpeed }.coerceAtLeast(0)
                if (idx > 0) {
                    _state.value = state.value.copy(scrollSpeed = steps[idx - 1])
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
            is SongViewerEvent.ToggleSpanishNotation -> {
                val newSpanish = !state.value.useSpanishNotation
                com.riffforge.core.domain.util.AppSettings.setSpanishNotation(newSpanish)
                _state.value = state.value.copy(useSpanishNotation = newSpanish)
                applyHarmonicShift(0, 0)
            }
            is SongViewerEvent.DeleteSong -> {
                val idToDelete = state.value.song?.id ?: return
                viewModelScope.launch {
                    songUseCases.deleteSong(idToDelete)
                }
            }
        }
    }

    private fun loadSong(id: Int, setId: Int?) {
        observeSongJob?.cancel()
        observeSongJob = songUseCases.observeSongById(id)
            .onEach { fetchedSong ->
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

                    val currentSpanish = com.riffforge.core.domain.util.AppSettings.useSpanishNotation
                    val totalShift = state.value.transposeSemitones - state.value.capo
                    var displayed = if (totalShift != 0) {
                        ChordTransposer.transposeContent(fetchedSong.content, totalShift, state.value.preferFlats)
                    } else {
                        fetchedSong.content
                    }
                    if (currentSpanish) {
                        displayed = ChordTransposer.convertNoteSystem(displayed, toSpanish = true)
                    }

                    _state.value = state.value.copy(
                        song = fetchedSong,
                        originalContent = fetchedSong.content,
                        displayedContent = displayed,
                        useSpanishNotation = currentSpanish,
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
            .launchIn(viewModelScope)
    }

    fun syncNotationWithGlobalSetting(useSpanish: Boolean) {
        if (state.value.useSpanishNotation != useSpanish) {
            _state.value = state.value.copy(useSpanishNotation = useSpanish)
            applyHarmonicShift(0, 0)
        }
    }

    private fun applyHarmonicShift(transposeDiff: Int, capoDiff: Int) {
        val newTranspose = state.value.transposeSemitones + transposeDiff
        val newCapo = state.value.capo + capoDiff
        val totalShift = newTranspose - newCapo

        var newContent = ChordTransposer.transposeContent(
            content = state.value.originalContent,
            semitones = totalShift,
            preferFlats = state.value.preferFlats
        )

        // Apply Spanish notation if enabled
        if (state.value.useSpanishNotation) {
            newContent = ChordTransposer.convertNoteSystem(newContent, toSpanish = true)
        }

        _state.value = state.value.copy(
            transposeSemitones = newTranspose,
            capo = newCapo,
            displayedContent = newContent
        )
    }
}