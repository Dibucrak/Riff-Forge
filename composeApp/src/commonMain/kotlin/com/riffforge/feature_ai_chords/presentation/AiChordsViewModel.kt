package com.riffforge.feature_ai_chords.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.riffforge.core.domain.store.RepertoireStore
import com.riffforge.feature_ai_chords.data.AiChordsRepository
import com.riffforge.feature_ai_chords.domain.AiChordsResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiChordsUiState(
    val input: String = "",
    val styleHint: String = "Original",
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: AiChordsResult? = null,
    val isSaved: Boolean = false
)

class AiChordsViewModel(
    private val repository: AiChordsRepository = AiChordsRepository()
) : ScreenModel {

    private val _state = MutableStateFlow(AiChordsUiState())
    val state: StateFlow<AiChordsUiState> = _state.asStateFlow()

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    fun onInputChanged(value: String) {
        _state.update { it.copy(input = value, error = null) }
    }

    fun onStyleChanged(styleHint: String) {
        _state.update { it.copy(styleHint = styleHint) }
    }

    fun applySample(sampleUrl: String) {
        _state.update { it.copy(input = sampleUrl, error = null) }
    }

    fun extractChords() {
        val query = _state.value.input.trim()
        if (query.isBlank()) {
            _state.update {
                it.copy(error = "Pega un enlace de YouTube o escribe el título de una canción.")
            }
            return
        }
        if (_state.value.isLoading) return

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isSaved = false) }
            val res = repository.extractChords(input = query, styleHint = _state.value.styleHint)
            res.fold(
                onSuccess = { extracted ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            result = extracted,
                            error = null,
                            isSaved = false
                        )
                    }
                },
                onFailure = { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "No se pudieron extraer los acordes."
                        )
                    }
                }
            )
        }
    }

    fun saveToRepertoire() {
        val current = _state.value.result ?: return
        if (_state.value.isSaved) return

        val alreadyExists = RepertoireStore.songs.any {
            it.title.trim().equals(current.title.trim(), ignoreCase = true) &&
                it.artist.trim().equals(current.artist.trim(), ignoreCase = true)
        }

        if (alreadyExists) {
            _state.update { it.copy(isSaved = true) }
            screenModelScope.launch {
                _snackbarFlow.emit("\"${current.title}\" ya está en Mi Repertorio.")
            }
            return
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

        RepertoireStore.saveSong(
            id = null,
            title = current.title,
            artist = current.artist,
            key = current.key,
            tuning = current.tuning,
            bpm = current.bpm,
            lyricsAndChords = fullContent
        )
        _state.update { it.copy(isSaved = true) }
        screenModelScope.launch {
            _snackbarFlow.emit("\"${current.title}\" guardada en Mi Repertorio.")
        }
    }
}
