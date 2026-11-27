package com.riffforge.feature_profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import com.riffforge.LocalAuthManager
import com.riffforge.LocalCloudSyncManager
import com.riffforge.LocalSignOutAction
import com.riffforge.core.domain.store.RepertoireStore
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.ThemeMode
import com.riffforge.core.presentation.components.ProfileAvatar
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.core.presentation.theme.isThemeDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigator: Navigator? = null,
    onNavigateUp: () -> Unit = { navigator?.pop() },
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToCloudBackup: () -> Unit = {}
) {
    val authManager = LocalAuthManager.current
    val syncManager = LocalCloudSyncManager.current
    val signOutAction = LocalSignOutAction.current
    val currentUser by authManager.currentUser.collectAsState(initial = null)
    val currentThemeMode = AppSettings.themeMode

    var isSyncing by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var isLinkingGoogle by remember { mutableStateOf(false) }
    var isUpdatingPhoto by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var usernameOverride by remember(currentUser?.uid) { mutableStateOf<String?>(null) }
    var photoDataOverride by remember(currentUser?.uid) { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val displayName = currentUser?.displayName?.ifBlank { "Guitarrista" } ?: "Guitarrista"
    val username = usernameOverride ?: currentUser?.username?.ifBlank { "" }.orEmpty()
    val email = currentUser?.email ?: ""
    val isAdmin = currentUser?.isAdmin == true
    val activeUid = currentUser?.uid?.takeIf { it.isNotBlank() }
    val photoUrl = currentUser?.photoUrl.orEmpty()
    val photoData = photoDataOverride ?: currentUser?.photoData.orEmpty()

    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdatingPhoto) showPhotoOptionsDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Foto de perfil", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (photoData.isNotBlank()) {
                            "Tienes una foto personalizada guardada en tu cuenta."
                        } else if (photoUrl.isNotBlank()) {
                            "Actualmente se muestra tu foto de perfil de Google."
                        } else {
                            "Elige una imagen de tu galería para personalizar tu perfil."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            val uid = activeUid
                            if (uid == null) {
                                showPhotoOptionsDialog = false
                                scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para cambiar tu foto.") }
                                return@Button
                            }
                            showPhotoOptionsDialog = false
                            scope.launch {
                                isUpdatingPhoto = true
                                val pickRes = syncManager.pickProfilePhoto()
                                pickRes.onSuccess { base64 ->
                                    if (!base64.isNullOrBlank()) {
                                        val updateRes = syncManager.updateProfilePhoto(uid, base64)
                                        updateRes.onSuccess {
                                            photoDataOverride = base64
                                            snackbarHostState.showSnackbar("Foto de perfil actualizada.")
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar(err.message ?: "No se pudo guardar la foto.")
                                        }
                                    }
                                }.onFailure { err ->
                                    snackbarHostState.showSnackbar(err.message ?: "No se pudo seleccionar la imagen.")
                                }
                                isUpdatingPhoto = false
                            }
                        },
                        enabled = !isUpdatingPhoto,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Elegir foto de galería", fontWeight = FontWeight.Bold)
                    }
                    if (photoData.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val uid = activeUid
                                if (uid == null) {
                                    showPhotoOptionsDialog = false
                                    return@OutlinedButton
                                }
                                showPhotoOptionsDialog = false
                                scope.launch {
                                    isUpdatingPhoto = true
                                    val updateRes = syncManager.updateProfilePhoto(uid, "")
                                    updateRes.onSuccess {
                                        photoDataOverride = ""
                                        val msg = if (photoUrl.isNotBlank()) {
                                            "Se restauró la foto de Google."
                                        } else {
                                            "Se quitó la foto personalizada."
                                        }
                                        snackbarHostState.showSnackbar(msg)
                                    }.onFailure { err ->
                                        snackbarHostState.showSnackbar(err.message ?: "No se pudo quitar la foto.")
                                    }
                                    isUpdatingPhoto = false
                                }
                            },
                            enabled = !isUpdatingPhoto,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (photoUrl.isNotBlank()) "Usar foto de Google" else "Quitar foto personalizada",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showPhotoOptionsDialog = false },
                    enabled = !isUpdatingPhoto
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showEditUsernameDialog) {
        var usernameInput by remember(username) { mutableStateOf(username) }
        AlertDialog(
            onDismissRequest = { showEditUsernameDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Editar nombre de usuario", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    Text(
                        text = "Visible en tus aportes de la Comunidad (3–24 caracteres: a-z, 0-9 o _).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        singleLine = true,
                        prefix = { Text("@") },
                        label = { Text("Usuario") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleaned = usernameInput.trim().removePrefix("@").lowercase()
                        val uid = activeUid
                        showEditUsernameDialog = false
                        if (uid == null) {
                            scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para editar.") }
                            return@Button
                        }
                        if (!cleaned.matches(Regex("^[a-z0-9_]{3,24}$"))) {
                            scope.launch { snackbarHostState.showSnackbar("Usa 3–24 caracteres (letras, números o _).") }
                            return@Button
                        }
                        scope.launch {
                            val res = syncManager.updateUsername(uid, cleaned)
                            res.onSuccess {
                                usernameOverride = cleaned
                                snackbarHostState.showSnackbar("Nombre de usuario actualizado.")
                            }.onFailure { err ->
                                snackbarHostState.showSnackbar(err.message ?: "Error al actualizar usuario")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUsernameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Restaurar desde la nube?", fontWeight = FontWeight.ExtraBold) },
            text = {
                Text(
                    text = "Se importarán tus canciones y setlists guardados en Firebase a este dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        val uid = activeUid
                        if (uid == null) {
                            scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para restaurar.") }
                            return@Button
                        }
                        scope.launch {
                            isSyncing = true
                            val res = syncManager.restoreCatalog(uid)
                            res.onSuccess { json ->
                                val importRes = runCatching { RepertoireStore.importBackupJson(json) }
                                importRes.onSuccess { (songsCount, setlistsCount) ->
                                    snackbarHostState.showSnackbar("Copia restaurada ($songsCount canciones, $setlistsCount setlists)")
                                }.onFailure { err ->
                                    snackbarHostState.showSnackbar(err.message ?: "Formato de respaldo inválido")
                                }
                            }.onFailure { err ->
                                snackbarHostState.showSnackbar(err.message ?: "Error al restaurar")
                            }
                            isSyncing = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restaurar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ── Delete Account 5-Second Countdown Dialog ─────────────────────────
    if (showDeleteAccountDialog) {
        var countdown by remember { mutableStateOf(5) }
        LaunchedEffect(Unit) {
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
        }

        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            title = {
                Text(
                    text = "¿Eliminar tu cuenta?",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará permanentemente tu cuenta, tus canciones privadas y tus setlists respaldados en la nube. No se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        scope.launch {
                            isDeletingAccount = true
                            val result = authManager.deleteAccount()
                            isDeletingAccount = false
                            result.onSuccess {
                                RepertoireStore.clearLocalRepertoire()
                                signOutAction()
                            }.onFailure { err ->
                                snackbarHostState.showSnackbar(err.message ?: "No se pudo eliminar la cuenta")
                            }
                        }
                    },
                    enabled = countdown == 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = if (countdown > 0) "Confirmar (${countdown}s)" else "Eliminar Cuenta",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Horizontal User Identity Card ────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(
                        photoData = photoData,
                        photoUrl = photoUrl,
                        size = 64.dp,
                        showEditBadge = true,
                        isLoading = isUpdatingPhoto,
                        onClick = { showPhotoOptionsDialog = true }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (username.isNotBlank()) {
                            Text(
                                text = "@$username",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (email.isNotBlank()) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { showEditUsernameDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar usuario",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Preferencias Globales ────────────────────────────────────
            Text(
                text = "Preferencias Globales",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val isAmerican = !AppSettings.useSpanishNotation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Notación Americana (C, D, E)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Desactivar para cifrado latino (Do, Re, Mi)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAmerican,
                            onCheckedChange = { american ->
                                AppSettings.setSpanishNotation(!american)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Apariencia y Tema",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val themeCards = listOf(
                            Triple(ThemeMode.SYSTEM, "Sistema", Pair(Color(0xFFFCF7F4), Color(0xFFC83700))),
                            Triple(ThemeMode.MOONLIGHT_DARK, "Moon Dark", Pair(Color(0xFF14151B), Color(0xFFC58FFF))),
                            Triple(ThemeMode.MOONLIGHT_LIGHT, "Moon Light", Pair(Color(0xFFEDEEF2), Color(0xFF9A5FD9))),
                            Triple(ThemeMode.MONOCHROME_DARK, "Mono Dark", Pair(Color(0xFF161618), Color(0xFF8C91A8))),
                            Triple(ThemeMode.MONOCHROME_LIGHT, "Mono Light", Pair(Color(0xFFF0F1F4), Color(0xFF5F6370))),
                            Triple(ThemeMode.RETRO_DARK, "Retro Dark", Pair(Color(0xFF1A1E22), Color(0xFF5C87A4))),
                            Triple(ThemeMode.RETRO_LIGHT, "Retro Light", Pair(Color(0xFFF3EFEA), Color(0xFF456A82)))
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            themeCards.forEach { (mode, label, colors) ->
                                val isSelected = currentThemeMode == mode
                                Surface(
                                    modifier = Modifier
                                        .width(96.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { AppSettings.updateThemeMode(mode) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(colors.first)
                                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.second)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Cuentas Vinculadas (Google) ──────────────────────────────
            val linkedProviders = currentUser?.linkedProviders ?: emptyList()
            val isGoogleLinked = "google.com" in linkedProviders

            Text(
                text = "Cuentas Vinculadas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4285F4)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGoogleLinked) "Cuenta vinculada" else "Accede también con tu cuenta de Google",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isGoogleLinked) SystemGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (isGoogleLinked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(SystemGreen.copy(alpha = 0.16f))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "Vinculada",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SystemGreen
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable(enabled = !isLinkingGoogle) {
                                    scope.launch {
                                        isLinkingGoogle = true
                                        val res = authManager.linkWithGoogle()
                                        isLinkingGoogle = false
                                        res.onSuccess {
                                            snackbarHostState.showSnackbar("Cuenta vinculada con Google exitosamente.")
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar(err.message ?: "No se pudo vincular con Google")
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLinkingGoogle) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Vincular",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Nube & Cuenta ────────────────────────────────────────────
            Text(
                text = "Nube & Cuenta",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Respaldo en la Nube",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Respalda tu repertorio privado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable(enabled = !isSyncing) {
                                    val uid = activeUid
                                    if (uid == null) {
                                        scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para respaldar.") }
                                        return@clickable
                                    }
                                    scope.launch {
                                        isSyncing = true
                                        val json = RepertoireStore.exportBackupJson()
                                        val res = syncManager.backupCatalog(uid, json)
                                        res.onSuccess {
                                            snackbarHostState.showSnackbar("Respaldo completado")
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar(err.message ?: "Error al respaldar")
                                        }
                                        isSyncing = false
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Sincronizar",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSyncing) {
                                if (activeUid == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Debes iniciar sesión para restaurar.") }
                                } else {
                                    showRestoreConfirmDialog = true
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Restaurar copia de seguridad",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Restaurar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isAdmin) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAdmin() }
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Panel de Administración",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Cerrar Sesión ────────────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        authManager.signOut()
                        RepertoireStore.clearLocalRepertoire()
                        signOutAction()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Eliminar Cuenta (5-second unlock dialog) ─────────────────
            TextButton(
                onClick = { showDeleteAccountDialog = true },
                enabled = !isDeletingAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isDeletingAccount) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.error,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Eliminando cuenta...",
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Eliminar Cuenta",
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
