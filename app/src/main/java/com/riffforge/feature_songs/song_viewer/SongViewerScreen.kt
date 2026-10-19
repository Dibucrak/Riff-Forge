package com.riffforge.feature_songs.presentation.song_viewer

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.components.GlassBackground
import com.riffforge.core.presentation.components.GlassCard
import com.riffforge.core.presentation.components.GlassCardElevated
import com.riffforge.core.presentation.components.GlassStatChip
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_songs.domain.util.ChordTransposer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun SongViewerScreen(
    onNavigateUp: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToSong: (Int, Int) -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: SongViewerViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight
    val edgeColors = if (isDarkTheme) {
        listOf(GlassEdgeDarkStart, GlassEdgeDarkMid, GlassEdgeDarkEnd)
    } else {
        listOf(GlassEdgeHighlightStart, GlassEdgeHighlightMid, GlassEdgeHighlightEnd)
    }
    val glassSurface = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight

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

    GlassBackground(isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top bar — glassmorphic header
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                isDarkTheme = isDarkTheme,
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateUp,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = glassSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = textColor)
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                        Text(
                            text = state.song?.title ?: "Cargando...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = state.song?.artist ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = dimTextColor
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(SongViewerEvent.ToggleFlats) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = glassSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Cambiar #/b",
                            tint = if (state.preferFlats) AccentTint else dimTextColor
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(SongViewerEvent.ToggleSpanishNotation) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (state.useSpanishNotation) AccentTint.copy(alpha = 0.3f) else glassSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = if (state.useSpanishNotation) "Do" else "C",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.useSpanishNotation) AccentTint else dimTextColor
                        )
                    }
                    IconButton(
                        onClick = { state.song?.id?.let { onNavigateToEdit(it) } },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = glassSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = textColor)
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentTint
                )
            } else if (state.song == null) {
                Text(
                    text = "No se pudo cargar la canción.",
                    color = SystemRed,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 112.dp, end = 16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Song info chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassStatChip(
                            isDarkTheme = isDarkTheme,
                            icon = {},
                            label = {
                                val shiftedNote = ChordTransposer.shiftNote(state.song.key, state.transposeSemitones, state.preferFlats)
                                val displayName = ChordTransposer.getNoteDisplayName(shiftedNote, state.useSpanishNotation)
                                Text("Tono: $displayName", fontWeight = FontWeight.Bold, color = textColor)
                            }
                        )
                        GlassStatChip(
                            isDarkTheme = isDarkTheme,
                            icon = {},
                            label = {
                                Text("Afinación: ${state.song.tuning}", color = textColor)
                            }
                        )
                        GlassStatChip(
                            isDarkTheme = isDarkTheme,
                            icon = {},
                            label = {
                                Text("${state.song.bpm} BPM", color = textColor)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = state.displayedContent.ifBlank { "Sin contenido (Edita la canción para añadir letra y acordes)." },
                        fontSize = state.textSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        color = textColor,
                        lineHeight = (state.textSizeSp * 1.5).sp
                    )

                    Spacer(modifier = Modifier.height(180.dp))
                }
            }

            // Bottom controls — glassmorphic panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                // Setlist navigation
                if (state.currentSetlistId != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        isDarkTheme = isDarkTheme,
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    state.previousSongId?.let { onNavigateToSong(it, state.currentSetlistId) }
                                },
                                enabled = state.previousSongId != null,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = glassSurface.copy(alpha = 0.3f),
                                    disabledContainerColor = glassSurface.copy(alpha = 0.1f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious, contentDescription = "Anterior",
                                    tint = if (state.previousSongId != null) AccentTint else dimTextColor.copy(alpha = 0.3f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentTint)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = state.setlistName ?: "Setlist",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                                Text(
                                    text = "Pista ${state.currentSongIndex + 1} de ${state.totalSongsInSet}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = dimTextColor
                                )
                            }

                            IconButton(
                                onClick = {
                                    state.nextSongId?.let { onNavigateToSong(it, state.currentSetlistId) }
                                },
                                enabled = state.nextSongId != null,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = glassSurface.copy(alpha = 0.3f),
                                    disabledContainerColor = glassSurface.copy(alpha = 0.1f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.SkipNext, contentDescription = "Siguiente",
                                    tint = if (state.nextSongId != null) AccentTint else dimTextColor.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                // Controls panel
                GlassCardElevated(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    isDarkTheme = isDarkTheme,
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Transpose & Capo row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Tono:", style = MaterialTheme.typography.labelMedium, color = dimTextColor)
                                IconButton(
                                    onClick = { viewModel.onEvent(SongViewerEvent.TransposeDown) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = glassSurface.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Bajar Tono", tint = textColor, modifier = Modifier.padding(4.dp))
                                }
                                Text(
                                    text = if (state.transposeSemitones > 0) "+${state.transposeSemitones}" else "${state.transposeSemitones}",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentTint
                                )
                                IconButton(
                                    onClick = { viewModel.onEvent(SongViewerEvent.TransposeUp) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = glassSurface.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Subir Tono", tint = textColor, modifier = Modifier.padding(4.dp))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Capo:", style = MaterialTheme.typography.labelMedium, color = dimTextColor)
                                IconButton(
                                    onClick = { viewModel.onEvent(SongViewerEvent.CapoDown) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = glassSurface.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Bajar Capo", tint = textColor, modifier = Modifier.padding(4.dp))
                                }
                                Text(
                                    text = "${state.capo}",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentTint
                                )
                                IconButton(
                                    onClick = { viewModel.onEvent(SongViewerEvent.CapoUp) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = glassSurface.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Subir Capo", tint = textColor, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = glassSurface.copy(alpha = 0.3f))

                        // Text size, AutoScroll, Speed row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                IconButton(
                                    onClick = { viewModel.onEvent(SongViewerEvent.DecreaseTextSize) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = glassSurface.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.TextDecrease, contentDescription = "Texto menor", tint = textColor)
                                }
                                IconButton(
                                    onClick = { viewModel.onEvent(SongViewerEvent.IncreaseTextSize) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = glassSurface.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.TextIncrease, contentDescription = "Texto mayor", tint = textColor)
                                }
                            }

                            // AutoScroll button — gradient FAB with press dimming
                            val autoScrollInteraction = remember { MutableInteractionSource() }
                            val autoScrollPressed = autoScrollInteraction.collectIsPressedAsState()
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .shadow(8.dp, CircleShape, spotColor = GlassShadow, ambientColor = GlassShadow)
                                    .clip(CircleShape)
                                    .background(
                                        AccentTint,
                                        CircleShape
                                    )
                                    .graphicsLayer {
                                        alpha = if (autoScrollPressed.value) 0.6f else 1f
                                    }
                                    .clickable(
                                        interactionSource = autoScrollInteraction,
                                        indication = null,
                                        onClick = { viewModel.onEvent(SongViewerEvent.ToggleAutoScroll) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (state.isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "AutoScroll",
                                    tint = OnAccent
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.onEvent(SongViewerEvent.DecreaseSpeed) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = glassSurface.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.FastRewind, contentDescription = "Más lento", tint = textColor)
                                }
                                Text(
                                    text = "${state.scrollSpeed}x",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentTint
                                )
                                IconButton(
                                    onClick = { viewModel.onEvent(SongViewerEvent.IncreaseSpeed) },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = glassSurface.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.FastForward, contentDescription = "Más rápido", tint = textColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}