package com.riffforge.feature_profile.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_auth.domain.use_case.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getUserJob: Job? = null

    sealed class UiEvent {
        object SignOutSuccess : UiEvent()
    }

    init {
        getCurrentUser()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.SignOut -> {
                viewModelScope.launch {
                    authUseCases.signOut()
                    _eventFlow.emit(UiEvent.SignOutSuccess)
                }
            }
        }
    }

    private fun getCurrentUser() {
        getUserJob?.cancel()
        getUserJob = authUseCases.getCurrentUser()
            .onEach { user ->
                if (user != null) {
                    _state.value = state.value.copy(
                        email = user.email,
                        displayName = user.displayName ?: "Guitarrista",
                        uid = user.uid,
                        isLoading = false
                    )
                } else {
                    _state.value = state.value.copy(isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }
}