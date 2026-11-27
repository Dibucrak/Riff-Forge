package com.riffforge.feature_auth.presentation.forgot_password

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_auth.domain.use_case.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _emailSent = mutableStateOf(false)
    val emailSent: State<Boolean> = _emailSent

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    fun sendResetLink(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authUseCases.sendPasswordReset(email)
            _isLoading.value = false
            result.onSuccess {
                _emailSent.value = true
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        "Te enviamos un enlace a ${email.trim()}. Revisa tu bandeja o spam."
                    )
                )
            }.onFailure { err ->
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(err.message ?: "No se pudo enviar el enlace de recuperación")
                )
            }
        }
    }
}
