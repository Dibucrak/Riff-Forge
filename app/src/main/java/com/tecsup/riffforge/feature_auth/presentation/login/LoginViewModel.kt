package com.riffforge.feature_auth.presentation.login

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
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    private val _isPasswordVisible = mutableStateOf(false)
    val isPasswordVisible: State<Boolean> = _isPasswordVisible

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object LoginSuccess : UiEvent()
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EnteredEmail -> {
                _state.value = state.value.copy(emailText = event.value)
            }
            is LoginEvent.EnteredPassword -> {
                _state.value = state.value.copy(passwordText = event.value)
            }
            is LoginEvent.TogglePasswordVisibility -> {
                _isPasswordVisible.value = !_isPasswordVisible.value
            }
            is LoginEvent.SignIn -> {
                viewModelScope.launch {
                    _state.value = state.value.copy(isLoading = true, error = null)

                    val result = authUseCases.signIn(
                        email = state.value.emailText.trim(),
                        password = state.value.passwordText.trim()
                    )

                    result.onSuccess {
                        _state.value = state.value.copy(isLoading = false)
                        _eventFlow.emit(UiEvent.LoginSuccess)
                    }.onFailure { exception ->
                        _state.value = state.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al iniciar sesión"
                        )
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = exception.message ?: "Credenciales inválidas"
                            )
                        )
                    }
                }
            }
        }
    }
}