package com.riffforge.feature_setlists.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.riffforge.core.domain.store.RepertoireStore
import com.riffforge.core.domain.store.SongItemModel
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistDetailScreen(
    setlistId: String? = null,
    onNavigateUp: () -> Unit = {},
    onSongClick: (songId: String, setlistId: String) -> Unit = { _, _ -> }
) {
    val useSpanish = AppSettings.useSpanishNotation
    val setlist = RepertoireStore.getSetlist(setlistId)
    val setlistSongs = RepertoireStore.getSongsForSetlist(setlist?.id)
    val availableSongs = RepertoireStore.songs.filter { candidate ->
        setlist?.songIds?.contains(candidate.id) != true
    }

    var showAddSongDialog by remember { mutableStateOf(false) }
    var showEditSetlistDialog by remember { mutableStateOf(false) }
    var showDeleteSetlistDialog by remember { mutableStateOf(false) }
    var songPendingRemove by remember { mutableStateOf<SongItemModel?>(null) }

    if (showEditSetlistDialog && setlist != null) {
        var editName by remember(setlist.id) { mutableStateOf(setlist.name) }
        var editDesc by remember(setlist.id) { mutableStateOf(setlist.description) }
        AlertDialog(
            onDismissRequest = { showEditSetlistDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Editar Setlist", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre del Setlist") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Descripción (Opcional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            RepertoireStore.updateSetlist(setlist.id, editName, editDesc)
                            showEditSetlistDialog = false
                        }
                    },
                    enabled = editName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Guardar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEditSetlistDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteSetlistDialog && setlist != null) {
        AlertDialog(
            onDismissRequest = { showDeleteSetlistDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar setlist?", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará \"${setlist.name}\". Tus canciones locales permanecerán intactas.") },
            confirmButton = {
                Button(
                    onClick = {
                        RepertoireStore.deleteSetlist(setlist.id)
                        showDeleteSetlistDialog = false
                        onNavigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSetlistDialog = false }) { Text("Cancelar") }
            }
        )
    }

    songPendingRemove?.let { target ->
        if (setlist != null) {
            AlertDialog(
                onDismissRequest = { songPendingRemove = null },
                shape = RoundedCornerShape(24.dp),
                title = { Text("¿Quitar del setlist?", fontWeight = FontWeight.Bold) },
                text = { Text("Se quitará \"${target.title}\" de \"${setlist.name}\" (seguirá en tu repertorio).") },
                confirmButton = {
                    Button(
                        onClick = {
                            RepertoireStore.removeSongFromSetlist(setlist.id, target.id)
                            songPendingRemove = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Quitar", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { songPendingRemove = null }) { Text("Cancelar") }
                }
            )
        }
    }

    if (showAddSongDialog && setlist != null) {
        AlertDialog(
            onDismissRequest = { showAddSongDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Añadir al Setlist", fontWeight = FontWeight.Bold) },
            text = {
                if (availableSongs.isEmpty()) {
                    Text(
                        text = "No hay más canciones disponibles para añadir.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(availableSongs, key = { it.id }) { song ->
                            ListItem(
                                headlineContent = { Text(song.title, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(song.artist) },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            RepertoireStore.addSongToSetlist(setlist.id, song.id)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Añadir",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddSongDialog = false }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Setlist", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (setlist != null) {
                        IconButton(onClick = { showEditSetlistDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Setlist", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showDeleteSetlistDialog = true }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar Setlist", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSongDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Canción al Setlist")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            setlist?.let { sl ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Header Summary Card ──────────────────────────────
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LibraryMusic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sl.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (sl.description.isNotBlank()) {
                                        Text(
                                            text = sl.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${setlistSongs.size} canciones en repertorio",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (setlistSongs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onSongClick(setlistSongs.first().id, sl.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Iniciar Ensayo Continuo", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Lista de Repertorio",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    if (setlistSongs.isEmpty()) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Este setlist está vacío",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toca el botón + para agregar canciones",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 96.dp)
                            ) {
                                itemsIndexed(setlistSongs, key = { _, s -> s.id }) { index, song ->
                                    val displayKey = NotationConverter.formatChord(song.key, useSpanish)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSongClick(song.id, sl.id) }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${song.artist} • $displayKey",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                songPendingRemove = song
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Quitar del setlist",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Abrir",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (index < setlistSongs.lastIndex) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                            modifier = Modifier.padding(start = 62.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
