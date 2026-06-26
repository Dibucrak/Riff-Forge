package com.riffforge.feature_setlists.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_setlists.domain.model.Setlist
import com.riffforge.feature_setlists.domain.use_case.SetlistUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetlistsViewModel @Inject constructor(
    private val setlistUseCases: SetlistUseCases
) : ViewModel() {

    private val _state = mutableStateOf(SetlistsState())
    val state: State<SetlistsState> = _state

    private var getSetlistsJob: Job? = null

    init {
        getSetlists()
    }

    fun onEvent(event: SetlistsEvent) {
        when (event) {
            is SetlistsEvent.ShowAddSetlistDialog -> {
                _state.value = state.value.copy(isAddingSetlist = true)
            }
            is SetlistsEvent.HideAddSetlistDialog -> {
                _state.value = state.value.copy(
                    isAddingSetlist = false,
                    newSetlistName = "",
                    newSetlistDescription = ""
                )
            }
            is SetlistsEvent.EnteredName -> {
                _state.value = state.value.copy(newSetlistName = event.name)
            }
            is SetlistsEvent.EnteredDescription -> {
                _state.value = state.value.copy(newSetlistDescription = event.description)
            }
            is SetlistsEvent.SaveSetlist -> {
                viewModelScope.launch {
                    try {
                        setlistUseCases.addSetlist(
                            Setlist(
                                name = state.value.newSetlistName,
                                description = state.value.newSetlistDescription
                            )
                        )
                        _state.value = state.value.copy(
                            isAddingSetlist = false,
                            newSetlistName = "",
                            newSetlistDescription = ""
                        )
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    private fun getSetlists() {
        getSetlistsJob?.cancel()
        getSetlistsJob = setlistUseCases.getSetlists()
            .onEach { setlists ->
                _state.value = state.value.copy(setlists = setlists)
            }
            .launchIn(viewModelScope)
    }
}