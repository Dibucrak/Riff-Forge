package com.riffforge.feature_songs.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.riffforge.core.presentation.components.ProfileAvatar
import com.riffforge.feature_setlists.domain.model.SetlistDetail
import com.riffforge.feature_setlists.presentation.SetlistsEvent
import com.riffforge.feature_setlists.presentation.SetlistsViewModel
import com.riffforge.feature_setlists.presentation.components.SetlistItem
import com.riffforge.feature_songs.domain.model.Song
import com.riffforge.feature_songs.presentation.components.SongItem
import kotlinx.coroutines.tasks.await

/**
 * Songs Screen matching the ProfileScreen visual language:
 * Clean TopAppBar, Inset ElevatedCards with 20-24dp continuous curves,
 * theme-matched segmented chips, and 50dp action buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    onNavigateToAddSong: () -> Unit,
    onNavigateToSetlistDetail: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSongViewer: (Int, Int) -> Unit,
    isDarkTheme: Boolean,
    songsViewModel: SongsViewModel = hiltViewModel(),
    setlistsViewModel: SetlistsViewModel = hiltViewModel()
) {
    val songsState = songsViewModel.state.value
    val setlistsState = setlistsViewModel.state.value

    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val headerUid = firebaseUser?.uid.orEmpty()
    val fallbackPhotoUrl = remember(firebaseUser) {
        val raw = firebaseUser?.providerData?.firstOrNull { it.providerId == "google.com" && it.photoUrl != null }?.photoUrl
            ?: firebaseUser?.photoUrl
        raw?.toString()?.replace("=s96-c", "=s400-c").orEmpty()
    }
    var headerPhotoUrl by remember(headerUid) { mutableStateOf(fallbackPhotoUrl) }
    var headerPhotoData by remember(headerUid) { mutableStateOf("") }

    LaunchedEffect(headerUid) {
        if (headerUid.isNotBlank()) {
            runCatching {
                val doc = FirebaseFirestore.getInstance().collection("users").document(headerUid).get().await()
                val fsUrl = doc.getString("photoUrl").orEmpty()
                if (headerPhotoUrl.isBlank() && fsUrl.isNotBlank()) headerPhotoUrl = fsUrl
                headerPhotoData = doc.getString("photoData").orEmpty()
            }
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var songPendingDelete by remember { mutableStateOf<Song?>(null) }
    var setlistPendingDelete by remember { mutableStateOf<SetlistDetail?>(null) }
    val tabs = listOf("Canciones", "Setlists")

    val filteredSongs = remember(songsState.songs, searchQuery) {
        if (searchQuery.isBlank()) songsState.songs
        else songsState.songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.key.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredSetlists = remember(setlistsState.setlists, searchQuery) {
        if (searchQuery.isBlank()) setlistsState.setlists
        else setlistsState.setlists.filter {
            it.setlist.name.contains(searchQuery, ignoreCase = true) ||
                it.setlist.description.contains(searchQuery, ignoreCase = true)
        }
    }

    songPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { songPendingDelete = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar canción local?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Se eliminará \"${target.title}\" de tu dispositivo y de cualquier setlist donde esté incluida.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        songsViewModel.onEvent(SongsEvent.DeleteSong(target))
                        songPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { songPendingDelete = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    setlistPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { setlistPendingDelete = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar setlist?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Se eliminará el setlist \"${target.setlist.name}\". Tus canciones locales se conservarán.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        setlistsViewModel.onEvent(SetlistsEvent.DeleteSetlist(target.setlist.id))
                        setlistPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { setlistPendingDelete = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Repertorio", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        ProfileAvatar(
                            photoData = headerPhotoData,
                            photoUrl = headerPhotoUrl,
                            size = 38.dp
                        )
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
                onClick = {
                    if (selectedTabIndex == 0) {
                        onNavigateToAddSong()
                    } else {
                        setlistsViewModel.onEvent(SetlistsEvent.ShowAddSetlistDialog)
                    }
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 86.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Segmented Category Selector (Estilo ProfileScreen Theme Selector) ──
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        val containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        val contentColor = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(containerColor)
                                .clickable { selectedTabIndex = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Search Bar ────────────────────────────────────────────
            if (songsState.songs.isNotEmpty() || setlistsState.setlists.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (selectedTabIndex == 0) "Buscar canción, artista o tono..."
                            else "Buscar setlist..."
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Content Area ───────────────────────────────────────────
            if (selectedTabIndex == 0) {
                if (songsState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (filteredSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Sin resultados para \"$searchQuery\"" else "No tienes canciones aún",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Prueba con otro título, artista o tono" else "Añade tu primer riff o canción para ensayar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        if (searchQuery.isNotBlank()) searchQuery = ""
                                        else onNavigateToAddSong()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(imageVector = if (searchQuery.isNotBlank()) Icons.Default.Clear else Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (searchQuery.isNotBlank()) "Limpiar búsqueda" else "Agregar Canción", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    val bottomNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottomNavPadding + 148.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredSongs, key = { it.id }) { song ->
                            SongItem(
                                song = song,
                                onDeleteClick = { songPendingDelete = song },
                                onClick = { onNavigateToSongViewer(song.id, -1) }
                            )
                        }
                    }
                }
            } else {
                if (filteredSetlists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Sin setlists para \"$searchQuery\"" else "No tienes setlists",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Intenta con otro término" else "Organiza el orden de tus canciones para conciertos y ensayos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        if (searchQuery.isNotBlank()) searchQuery = ""
                                        else setlistsViewModel.onEvent(SetlistsEvent.ShowAddSetlistDialog)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(imageVector = if (searchQuery.isNotBlank()) Icons.Default.Clear else Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (searchQuery.isNotBlank()) "Limpiar búsqueda" else "Crear Setlist", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    val bottomNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottomNavPadding + 148.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredSetlists, key = { it.setlist.id }) { setlistDetail ->
                            SetlistItem(
                                setlistDetail = setlistDetail,
                                onDeleteClick = { setlistPendingDelete = setlistDetail },
                                onClick = { onNavigateToSetlistDetail(setlistDetail.setlist.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (setlistsState.isAddingSetlist) {
        AlertDialog(
            onDismissRequest = { setlistsViewModel.onEvent(SetlistsEvent.HideAddSetlistDialog) },
            title = {
                Text(
                    "Nuevo Setlist",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = setlistsState.newSetlistName,
                        onValueChange = { setlistsViewModel.onEvent(SetlistsEvent.EnteredName(it)) },
                        label = { Text("Nombre del Setlist") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = setlistsState.newSetlistDescription,
                        onValueChange = { setlistsViewModel.onEvent(SetlistsEvent.EnteredDescription(it)) },
                        label = { Text("Descripción (Opcional)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { setlistsViewModel.onEvent(SetlistsEvent.SaveSetlist) },
                    enabled = setlistsState.newSetlistName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { setlistsViewModel.onEvent(SetlistsEvent.HideAddSetlistDialog) }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
