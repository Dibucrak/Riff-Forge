package com.riffforge.feature_auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.components.GlassBackground
import com.riffforge.core.presentation.components.GlassButtonAccent
import com.riffforge.core.presentation.components.GlassCard
import com.riffforge.core.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val isPasswordVisible = viewModel.isPasswordVisible.value
    val snackbarHostState = remember { SnackbarHostState() }
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LoginViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                is LoginViewModel.UiEvent.LoginSuccess -> {
                    onNavigateToHome()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        GlassBackground(isDarkTheme = isDarkTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AccentTint.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Riff Forge Logo",
                            tint = AccentTint,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Riff Forge",
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Tu forja de acordes y setlists",
                        style = MaterialTheme.typography.bodyMedium,
                        color = dimTextColor
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Glass card form
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        isDarkTheme = isDarkTheme,
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                value = state.emailText,
                                onValueChange = { newText: String -> viewModel.onEvent(LoginEvent.EnteredEmail(newText)) },
                                label = { Text("Correo electrónico", color = dimTextColor) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = dimTextColor) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.passwordText,
                                onValueChange = { newText: String -> viewModel.onEvent(LoginEvent.EnteredPassword(newText)) },
                                label = { Text("Contraseña", color = dimTextColor) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = dimTextColor) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { viewModel.onEvent(LoginEvent.TogglePasswordVisibility) }) {
                                        Icon(imageVector = image, contentDescription = "Alternar visibilidad", tint = dimTextColor)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    color = AccentTint,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                GlassButtonAccent(
                                    onClick = { viewModel.onEvent(LoginEvent.SignIn) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Iniciar Sesión", fontWeight = FontWeight.Bold, color = textColor)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            text = "¿No tienes cuenta? Regístrate aquí",
                            fontWeight = FontWeight.SemiBold,
                            color = AccentTint
                        )
                    }
                }
            }
        }
    }
}