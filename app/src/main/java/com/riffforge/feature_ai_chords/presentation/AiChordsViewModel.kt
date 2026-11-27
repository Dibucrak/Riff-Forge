package com.riffforge.feature_ai_chords.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_ai_chords.domain.repository.AiChordsRepository
import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiChordsViewModel @Inject constructor(
    private val repository: AiChordsRepository,
    private val songUseCases: SongUseCases
) : ViewModel() {

    private val _state = mutableStateOf(AiChordsState())
    val state: State<AiChordsState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    fun onEvent(event: AiChordsEvent) {
        when (event) {
            is AiChordsEvent.InputChanged -> {
                _state.value = state.value.copy(
                    input = event.value,
                    error = null
                )
            }
            is AiChordsEvent.StyleChanged -> {
                _state.value = state.value.copy(styleHint = event.styleHint)
            }
            is AiChordsEvent.ApplySample -> {
                _state.value = state.value.copy(
                    input = event.sampleInput,
                    error = null
                )
            }
            is AiChordsEvent.ExtractChords -> extractChords()
            is AiChordsEvent.SaveToRepertoire -> saveToRepertoire()
        }
    }

    private fun extractChords() {
        val query = state.value.input.trim()
        if (query.isBlank()) {
            _state.value = state.value.copy(
                error = "Pega un enlace de YouTube o escribe el título de una canción."
            )
            return
        }
        if (state.value.isLoading) return

        viewModelScope.launch {
            _state.value = state.value.copy(
                isLoading = true,
                error = null,
                isSaved = false
            )
            val res = repository.extractChords(
                input = query,
                styleHint = state.value.styleHint
            )
            res.fold(
                onSuccess = { extracted ->
                    _state.value = state.value.copy(
                        isLoading = false,
                        result = extracted,
                        error = null,
                        isSaved = false
                    )
                },
                onFailure = { err ->
                    _state.value = state.value.copy(
                        isLoading = false,
                        error = err.message ?: "No se pudieron extraer los acordes."
                    )
                }
            )
        }
    }

    private fun saveToRepertoire() {
        val current = state.value.result ?: return
        if (state.value.isSaved) return

        viewModelScope.launch {
            try {
                val existing = songUseCases.getSongs().first()
                val alreadyExists = existing.any {
                    it.title.trim().equals(current.title.trim(), ignoreCase = true) &&
                        it.artist.trim().equals(current.artist.trim(), ignoreCase = true)
                }
                if (alreadyExists) {
                    _state.value = state.value.copy(isSaved = true)
                    _eventFlow.emit(UiEvent.ShowSnackbar("\"${current.title}\" ya está en Mi Repertorio."))
                    return@launch
                }

                val fullContent = buildString {
                    if (current.capo > 0) {
                        append("# Capo: Traste ${current.capo}\n")
                    }
                    if (current.strummingPattern.isNotBlank()) {
                        append("# Rasgueo: ${current.strummingPattern}\n")
                    }
                    if (current.youtubeUrl.isNotBlank()) {
                        append("# Fuente: ${current.youtubeUrl}\n")
                    }
                    if (isNotEmpty()) append("\n")
                    append(current.content)
                }

                songUseCases.addSong(
                    Song(
                        title = current.title,
                        artist = current.artist,
                        key = current.key,
                        tuning = current.tuning,
                        bpm = current.bpm,
                        content = fullContent,
                        isDraft = false
                    )
                )
                _state.value = state.value.copy(isSaved = true)
                _eventFlow.emit(UiEvent.ShowSnackbar("\"${current.title}\" guardada en Mi Repertorio."))
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error al guardar: ${e.message ?: "intenta de nuevo"}"))
            }
        }
    }
}
