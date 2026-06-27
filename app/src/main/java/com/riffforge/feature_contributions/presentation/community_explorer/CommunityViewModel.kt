package com.riffforge.feature_contributions.presentation.community_explorer

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_contributions.domain.repository.CommunityRepository
import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.domain.use_case.SongUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val songUseCases: SongUseCases
) : ViewModel() {

    private val _state = mutableStateOf(CommunityState())
    val state: State<CommunityState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getContributionsJob: Job? = null

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        getContributions()
    }

    fun onEvent(event: CommunityEvent) {
        when (event) {
            is CommunityEvent.SearchQueryChanged -> {
                _state.value = state.value.copy(searchQuery = event.query)
            }
            is CommunityEvent.DownloadContribution -> {
                downloadToLocalLibrary(event.contribution)
            }
        }
    }

    private fun getContributions() {
        getContributionsJob?.cancel()
        getContributionsJob = communityRepository.getApprovedContributions()
            .onEach { contributionsList ->
                _state.value = state.value.copy(
                    contributions = contributionsList,
                    isLoading = false,
                    error = ""
                )
            }
            .launchIn(viewModelScope)
    }

    private fun downloadToLocalLibrary(contribution: com.riffforge.feature_contributions.domain.model.Contribution) {
        viewModelScope.launch {
            try {
                val localSong = Song(
                    title = contribution.title,
                    artist = contribution.artist,
                    key = contribution.key,
                    tuning = contribution.tuning,
                    bpm = contribution.bpm,
                    content = contribution.content,
                    isDraft = false
                )

                songUseCases.addSong(localSong)
                _eventFlow.emit(UiEvent.ShowSnackbar("¡Descargada a tu repertorio local!"))

            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error al descargar: ${e.message}"))
            }
        }
    }
}