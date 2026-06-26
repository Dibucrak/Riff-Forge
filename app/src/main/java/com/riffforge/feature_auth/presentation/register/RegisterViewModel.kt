package com.riffforge.feature_auth.presentation.register

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
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _state = mutableStateOf(RegisterState())
    val state: State<RegisterState> = _state

    private val _isPasswordVisible = mutableStateOf(false)
    val isPasswordVisible: State<Boolean> = _isPasswordVisible

    private val _isRepeatedPasswordVisible = mutableStateOf(false)
    val isRepeatedPasswordVisible: State<Boolean> = _isRepeatedPasswordVisible

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object RegisterSuccess : UiEvent()
    }

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.EnteredDisplayName -> {
                _state.value = state.value.copy(displayNameText = event.value)
            }
            is RegisterEvent.EnteredEmail -> {
                _state.value = state.value.copy(emailText = event.value)
            }
            is RegisterEvent.EnteredPassword -> {
                _state.value = state.value.copy(passwordText = event.value)
            }
            is RegisterEvent.EnteredRepeatedPassword -> {
                _state.value = state.value.copy(repeatedPasswordText = event.value)
            }
            is RegisterEvent.TogglePasswordVisibility -> {
                _isPasswordVisible.value = !_isPasswordVisible.value
            }
            is RegisterEvent.ToggleRepeatedPasswordVisibility -> {
                _isRepeatedPasswordVisible.value = !_isRepeatedPasswordVisible.value
            }
            is RegisterEvent.SignUp -> {
                viewModelScope.launch {
                    val currentState = state.value

                    if (currentState.passwordText != currentState.repeatedPasswordText) {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Las contraseñas no coinciden"))
                        return@launch
                    }

                    _state.value = currentState.copy(isLoading = true, error = null)

                    val result = authUseCases.signUp(
                        email = currentState.emailText.trim(),
                        password = currentState.passwordText.trim(),
                        displayName = currentState.displayNameText.trim()
                    )

                    result.onSuccess {
                        _state.value = state.value.copy(isLoading = false)
                        _eventFlow.emit(UiEvent.ShowSnackbar("Cuenta creada exitosamente"))
                        _eventFlow.emit(UiEvent.RegisterSuccess)
                    }.onFailure { exception ->
                        _state.value = state.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al registrar usuario"
                        )
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = exception.message ?: "Error en el registro"
                            )
                        )
                    }
                }
            }
        }
    }
}