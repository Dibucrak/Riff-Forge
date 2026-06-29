package com.riffforge.feature_songs.presentation.song_viewer

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.feature_songs.domain.util.ChordTransposer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongViewerScreen(
    onNavigateUp: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToSong: (Int, Int) -> Unit,
    viewModel: SongViewerViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(state.isAutoScrolling, state.scrollSpeed) {
        if (state.isAutoScrolling) {
            while (isActive) {
                val currentScroll = scrollState.value
                val maxScroll = scrollState.maxValue
                if (currentScroll < maxScroll) {
                    scrollState.animateScrollTo(
                        value = currentScroll + (state.scrollSpeed * 10),
                        animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                    )
                } else {
                    viewModel.onEvent(SongViewerEvent.ToggleAutoScroll)
                    break
                }
                delay(10)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.song?.title ?: "Cargando...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.song?.artist ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(SongViewerEvent.ToggleFlats) }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Cambiar #/b",
                            tint = if (state.preferFlats) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = { state.song?.id?.let { onNavigateToEdit(it) } }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent)) {

                if (state.currentSetlistId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                state.previousSongId?.let { onNavigateToSong(it, state.currentSetlistId) }
                            },
                            enabled = state.previousSongId != null
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = if (state.previousSongId != null) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.setlistName ?: "Setlist",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Text(
                                text = "Pista ${state.currentSongIndex + 1} de ${state.totalSongsInSet}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(
                            onClick = {
                                state.nextSongId?.let { onNavigateToSong(it, state.currentSetlistId) }
                            },
                            enabled = state.nextSongId != null
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = if (state.nextSongId != null) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Tono:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.TransposeDown) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Bajar Tono", modifier = Modifier.padding(4.dp))
                                }
                                Text(
                                    text = if (state.transposeSemitones > 0) "+${state.transposeSemitones}" else "${state.transposeSemitones}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.TransposeUp) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Subir Tono", modifier = Modifier.padding(4.dp))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Capo:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.CapoDown) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Bajar Capo", modifier = Modifier.padding(4.dp))
                                }
                                Text(
                                    text = "${state.capo}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.CapoUp) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Subir Capo", modifier = Modifier.padding(4.dp))
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.DecreaseTextSize) }) {
                                    Icon(Icons.Default.TextDecrease, contentDescription = "Texto menor")
                                }
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.IncreaseTextSize) }) {
                                    Icon(Icons.Default.TextIncrease, contentDescription = "Texto mayor")
                                }
                            }

                            FloatingActionButton(
                                onClick = { viewModel.onEvent(SongViewerEvent.ToggleAutoScroll) },
                                containerColor = if (state.isAutoScrolling) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "AutoScroll"
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.DecreaseSpeed) }) {
                                    Icon(Icons.Default.FastRewind, contentDescription = "Más lento")
                                }
                                Text(
                                    text = "${state.scrollSpeed}x",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.IncreaseSpeed) }) {
                                    Icon(Icons.Default.FastForward, contentDescription = "Más rápido")
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.song == null) {
                Text(
                    text = "No se pudo cargar la canción.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val displayKey = ChordTransposer.shiftNote(state.song.key, state.transposeSemitones, state.preferFlats)
                        Text("Tono: $displayKey", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Afinación: ${state.song.tuning}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tempo: ${state.song.bpm} BPM", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = state.displayedContent.ifBlank { "Sin contenido (Edita la canción para añadir letra y acordes)." },
                        fontSize = state.textSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = (state.textSizeSp * 1.5).sp
                    )

                    Spacer(modifier = Modifier.height(180.dp))
                }
            }
        }
    }
}