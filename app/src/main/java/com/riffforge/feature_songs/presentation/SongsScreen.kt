package com.riffforge.feature_songs.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.feature_setlists.presentation.SetlistsEvent
import com.riffforge.feature_setlists.presentation.SetlistsViewModel
import com.riffforge.feature_setlists.presentation.components.SetlistItem
import com.riffforge.feature_songs.presentation.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    onNavigateToAddSong: () -> Unit,
    onNavigateToSetlistDetail: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSongViewer: (Int, Int) -> Unit,
    songsViewModel: SongsViewModel = hiltViewModel(),
    setlistsViewModel: SetlistsViewModel = hiltViewModel()
) {
    val songsState = songsViewModel.state.value
    val setlistsState = setlistsViewModel.state.value

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Canciones", "Setlists")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Mi Repertorio") },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
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
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedTabIndex == 0) {
                if (songsState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (songsState.songs.isEmpty()) {
                    Text(
                        text = "No tienes canciones guardadas.\n¡Añade tu primer riff!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(songsState.songs) { song ->
                            SongItem(
                                song = song,
                                onClick = { onNavigateToSongViewer(song.id, -1) }
                            )
                        }
                    }
                }
            } else {
                if (setlistsState.setlists.isEmpty()) {
                    Text(
                        text = "No tienes setlists.\n¡Crea uno para organizar tus ensayos!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(setlistsState.setlists) { setlistDetail ->
                            SetlistItem(
                                setlistDetail = setlistDetail,
                                onClick = {
                                    onNavigateToSetlistDetail(setlistDetail.setlist.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (setlistsState.isAddingSetlist) {
            AlertDialog(
                onDismissRequest = { setlistsViewModel.onEvent(SetlistsEvent.HideAddSetlistDialog) },
                title = { Text("Nuevo Setlist") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = setlistsState.newSetlistName,
                            onValueChange = { setlistsViewModel.onEvent(SetlistsEvent.EnteredName(it)) },
                            label = { Text("Nombre del Setlist") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = setlistsState.newSetlistDescription,
                            onValueChange = { setlistsViewModel.onEvent(SetlistsEvent.EnteredDescription(it)) },
                            label = { Text("Descripción (Opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { setlistsViewModel.onEvent(SetlistsEvent.SaveSetlist) },
                        enabled = setlistsState.newSetlistName.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { setlistsViewModel.onEvent(SetlistsEvent.HideAddSetlistDialog) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}