package com.riffforge.feature_song_editor.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riffforge.LocalAuthManager
import com.riffforge.LocalCloudSyncManager
import com.riffforge.core.domain.store.RepertoireStore
import com.riffforge.core.domain.store.SongItemModel
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import kotlinx.coroutines.launch

private val SONG_KEYS = listOf(
    "C", "Cm", "C#", "D", "Dm", "Eb", "E", "Em", "F", "Fm", "F#", "F#m",
    "G", "Gm", "Ab", "A", "Am", "Bb", "B", "Bm"
)

private val SONG_TUNINGS = listOf(
    "E Standard",
    "Drop D",
    "Half Step Down (Eb)",
    "Full Step Down (D)",
    "Drop C",
    "Open G",
    "DADGAD"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSongScreen(
    songId: String? = null,
    onNavigateUp: () -> Unit = {}
) {
    val useSpanish = AppSettings.useSpanishNotation
    val authManager = LocalAuthManager.current
    val syncManager = LocalCloudSyncManager.current
    val currentUser by authManager.currentUser.collectAsState(initial = null)

    val existingSong = remember(songId) {
        if (songId != null) RepertoireStore.getSong(songId) else null
    }

    var title by remember { mutableStateOf(existingSong?.title ?: "") }
    var artist by remember { mutableStateOf(existingSong?.artist ?: "") }
    var key by remember { mutableStateOf(existingSong?.key ?: "C") }
    var tuning by remember { mutableStateOf(existingSong?.tuning ?: "E Standard") }
    var bpmText by remember { mutableStateOf((existingSong?.bpm ?: 120).toString()) }
    var lyricsAndChords by remember { mutableStateOf(existingSong?.lyricsAndChords ?: "") }

    var keyMenuExpanded by remember { mutableStateOf(false) }
    var tuningMenuExpanded by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val saveAction: () -> Unit = {
        if (title.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar("El título de la canción no puede estar vacío")
            }
        } else {
            val bpm = bpmText.toIntOrNull()?.coerceIn(30, 300) ?: 120
            RepertoireStore.saveSong(
                id = existingSong?.id,
                title = title,
                artist = artist,
                key = key,
                tuning = tuning,
                bpm = bpm,
                lyricsAndChords = lyricsAndChords
            )
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (existingSong == null) "Nueva Canción" else "Editar Canción",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateUp) {
                        Text(
                            text = "Cancelar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    TextButton(onClick = saveAction) {
                        Text(
                            text = "Guardar",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Card 1: Título & Artista ─────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "Título",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (title.isEmpty()) {
                                    Text(
                                        text = "Ej: Flaca",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 17.sp
                                    )
                                }
                                inner()
                            }
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
                            text = "Artista",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicTextField(
                            value = artist,
                            onValueChange = { artist = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (artist.isEmpty()) {
                                    Text(
                                        text = "Ej: Andrés Calamaro",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 17.sp
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card 2: Tono, Afinación y Tempo ──────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { keyMenuExpanded = true }
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tono",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = NotationConverter.formatChord(key.ifBlank { "C" }, useSpanish),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Elegir tono",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = keyMenuExpanded,
                            onDismissRequest = { keyMenuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            SONG_KEYS.forEach { k ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = NotationConverter.formatChord(k, useSpanish),
                                            fontWeight = if (k == key) FontWeight.Bold else FontWeight.Normal,
                                            color = if (k == key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        key = k
                                        keyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tuningMenuExpanded = true }
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Afinación",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tuning.ifBlank { "E Standard" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Elegir afinación",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = tuningMenuExpanded,
                            onDismissRequest = { tuningMenuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            SONG_TUNINGS.forEach { t ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = t,
                                            fontWeight = if (t == tuning) FontWeight.Bold else FontWeight.Normal,
                                            color = if (t == tuning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        tuning = t
                                        tuningMenuExpanded = false
                                    }
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
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tempo (BPM)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        BasicTextField(
                            value = bpmText,
                            onValueChange = { newVal ->
                                if (newVal.all { it.isDigit() } && newVal.length <= 3) {
                                    bpmText = newVal
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Letra y Acordes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Monoespacio",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                BasicTextField(
                    value = lyricsAndChords,
                    onValueChange = { lyricsAndChords = it },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    decorationBox = { inner ->
                        if (lyricsAndChords.isEmpty()) {
                            Text(
                                text = "[Estribillo]\nG        B7\nFlaca, no me claves\nEm       C\ntus puñales por la espalda...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                        inner()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = saveAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar en Mi Repertorio", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    if (isPublishing) return@OutlinedButton
                    if (title.isBlank() || lyricsAndChords.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("El título y el contenido son obligatorios para publicar.")
                        }
                        return@OutlinedButton
                    }
                    val user = currentUser
                    if (user == null || user.uid.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Debes iniciar sesión para publicar en la comunidad.")
                        }
                        return@OutlinedButton
                    }
                    isPublishing = true
                    scope.launch {
                        val bpm = bpmText.toIntOrNull()?.coerceIn(30, 300) ?: 120
                        val model = SongItemModel(
                            id = existingSong?.id ?: "",
                            title = title.trim(),
                            artist = artist.trim().ifBlank { "Artista Desconocido" },
                            bpm = bpm,
                            key = key.ifBlank { "C" },
                            tuning = tuning.ifBlank { "E Standard" },
                            lyricsAndChords = lyricsAndChords
                        )
                        val authorName = user.username.takeIf { it.isNotBlank() }
                            ?: user.displayName.takeIf { it.isNotBlank() }
                            ?: "Guitarrista Riff Forge"
                        val result = syncManager.publishContribution(model, user.uid, authorName)
                        isPublishing = false
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("¡Aporte enviado para revisión de un administrador!")
                        } else {
                            snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Error al publicar.")
                        }
                    }
                },
                enabled = !isPublishing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPublishing) "Enviando..." else "Publicar en la Comunidad",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
