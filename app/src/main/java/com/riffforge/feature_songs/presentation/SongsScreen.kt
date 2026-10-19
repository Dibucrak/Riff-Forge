package com.riffforge.feature_songs.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_setlists.presentation.SetlistsEvent
import com.riffforge.feature_setlists.presentation.SetlistsViewModel
import com.riffforge.feature_songs.presentation.components.SongItem
import com.riffforge.feature_setlists.presentation.components.SetlistItem

/**
 * Liquid Glass Songs Screen — glassmorphic tabs, glass cards per song,
 * floating glass FAB, and empty state glass panel.
 */
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

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Canciones", "Setlists")

    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight

    GlassBackground(isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Glass header with tabs
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                isDarkTheme = isDarkTheme,
                cornerRadius = 20.dp,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mi Repertorio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = dimTextColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Glass tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        val bg = if (isSelected) {
                            if (isDarkTheme) GlassSurfaceElevatedDark else GlassSurfaceElevatedLight
                        } else {
                            Color.Transparent
                        }
                        val tabTextColor = if (isSelected) textColor else dimTextColor

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(bg, RoundedCornerShape(50.dp))
                                .clickable { selectedTabIndex = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = tabTextColor
                            )
                        }
                    }
                }
            }

            // Content area - below header (solid, nothing overlaps)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 140.dp, start = 16.dp, end = 16.dp, bottom = 72.dp)
            ) {
                if (selectedTabIndex == 0) {
                    if (songsState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentTint)
                        }
                    } else if (songsState.songs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                isDarkTheme = isDarkTheme,
                                cornerRadius = 24.dp
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No tienes canciones guardadas.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "¡Añade tu primer riff!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = dimTextColor
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(0.dp),
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                isDarkTheme = isDarkTheme,
                                cornerRadius = 24.dp
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No tienes setlists.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "¡Crea uno para organizar tus ensayos!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = dimTextColor
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(0.dp),
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

            // Floating Glass FAB
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .shadow(12.dp, CircleShape, spotColor = GlassShadow, ambientColor = GlassShadow)
                        .background(
                            AccentTint,
                            CircleShape
                        )
                        .clickable {
                            if (selectedTabIndex == 0) {
                                onNavigateToAddSong()
                            } else {
                                setlistsViewModel.onEvent(SetlistsEvent.ShowAddSetlistDialog)
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir",
                        tint = OnAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    if (setlistsState.isAddingSetlist) {
        AlertDialog(
            onDismissRequest = { setlistsViewModel.onEvent(SetlistsEvent.HideAddSetlistDialog) },
            title = { Text("Nuevo Setlist", color = textColor) },
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