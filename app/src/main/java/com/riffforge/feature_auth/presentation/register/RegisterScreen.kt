package com.riffforge.feature_auth.presentation.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun RegisterScreen(
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val isPasswordVisible = viewModel.isPasswordVisible.value
    val isRepeatedPasswordVisible = viewModel.isRepeatedPasswordVisible.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is RegisterViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                is RegisterViewModel.UiEvent.RegisterSuccess -> {
                    onNavigateUp()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkTheme = isDarkTheme,
                cornerRadius = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateUp,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isDarkTheme) SecondarySystemBackgroundDark.copy(alpha = 0.3f) else SystemGroupedBackgroundLight.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crear Cuenta",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        GlassBackground(isDarkTheme = isDarkTheme) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = 24.dp, top = 72.dp, end = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Únete a la comunidad",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Crea tu cuenta y comparte tu música",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dimTextColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDarkTheme = isDarkTheme,
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.displayNameText,
                            onValueChange = { newText: String -> viewModel.onEvent(RegisterEvent.EnteredDisplayName(newText)) },
                            label = { Text("Nombre de usuario", color = dimTextColor) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = dimTextColor) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.emailText,
                            onValueChange = { newText: String -> viewModel.onEvent(RegisterEvent.EnteredEmail(newText)) },
                            label = { Text("Correo electrónico", color = dimTextColor) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = dimTextColor) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.passwordText,
                            onValueChange = { newText: String -> viewModel.onEvent(RegisterEvent.EnteredPassword(newText)) },
                            label = { Text("Contraseña (mín. 6)", color = dimTextColor) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = dimTextColor) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { viewModel.onEvent(RegisterEvent.TogglePasswordVisibility) }) {
                                    Icon(imageVector = image, contentDescription = "Alternar visibilidad", tint = dimTextColor)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.repeatedPasswordText,
                            onValueChange = { newText: String -> viewModel.onEvent(RegisterEvent.EnteredRepeatedPassword(newText)) },
                            label = { Text("Confirmar contraseña", color = dimTextColor) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = dimTextColor) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = if (isRepeatedPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (isRepeatedPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { viewModel.onEvent(RegisterEvent.ToggleRepeatedPasswordVisibility) }) {
                                    Icon(imageVector = image, contentDescription = "Alternar visibilidad", tint = dimTextColor)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
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
                                onClick = { viewModel.onEvent(RegisterEvent.SignUp) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Registrarme", fontWeight = FontWeight.Bold, color = textColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}