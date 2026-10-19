package com.riffforge.feature_admin.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_admin.domain.repository.UserRepository
import com.riffforge.feature_contributions.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = mutableStateOf(AdminState())
    val state: State<AdminState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getPendingJob: Job? = null
    private var getUsersJob: Job? = null
    var deleteTargetUid: String? = null

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        getPendingContributions()
        getAllUsers()
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            is AdminEvent.ApproveContribution -> {
                viewModelScope.launch {
                    val result = communityRepository.approveContribution(event.id)
                    if (result.isSuccess) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Aporte aprobado y publicado."))
                    } else {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Error al aprobar."))
                    }
                }
            }
            is AdminEvent.RejectContribution -> {
                viewModelScope.launch {
                    val result = communityRepository.rejectContribution(event.id)
                    if (result.isSuccess) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Aporte rechazado y eliminado."))
                    } else {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Error al rechazar."))
                    }
                }
            }
            is AdminEvent.SelectTab -> {
                _state.value = state.value.copy(selectedTab = event.index)
            }
            is AdminEvent.ToggleAdminRole -> {
                viewModelScope.launch {
                    val result = userRepository.updateAdminRole(event.uid, event.isAdmin)
                    if (result.isSuccess) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            if (event.isAdmin) "Permiso de administrador otorgado." else "Permiso de administrador revocado."
                        ))
                    } else {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Error al actualizar el rol."))
                    }
                }
            }
            is AdminEvent.DeleteUser -> {
                deleteTargetUid = event.uid
            }
            is AdminEvent.SetDeleteTarget -> {
                deleteTargetUid = event.uid
            }
            AdminEvent.ConfirmDeleteUser -> {
                val uid = deleteTargetUid ?: return@onEvent
                viewModelScope.launch {
                    val result = userRepository.deleteUser(uid)
                    if (result.isSuccess) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Usuario eliminado."))
                        deleteTargetUid = null
                    } else {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Error al eliminar el usuario."))
                    }
                }
            }
        }
    }

    private fun getPendingContributions() {
        getPendingJob?.cancel()
        getPendingJob = communityRepository.getPendingContributions()
            .onEach { pendingList ->
                _state.value = state.value.copy(
                    pendingContributions = pendingList,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    private fun getAllUsers() {
        getUsersJob?.cancel()
        getUsersJob = userRepository.getAllUsers()
            .onEach { usersList ->
                _state.value = state.value.copy(
                    users = usersList,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
}