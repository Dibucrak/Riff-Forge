package com.riffforge.feature_songs.presentation.song_viewer

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.theme.SystemRed
import com.riffforge.feature_songs.domain.util.ChordTransposer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
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
    val globalUseSpanish = com.riffforge.core.domain.util.AppSettings.useSpanishNotation
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(globalUseSpanish) {
        viewModel.syncNotationWithGlobalSetting(globalUseSpanish)
    }

    if (showDeleteDialog && state.song != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar canción local?", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará \"${state.song.title}\" de tu dispositivo.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(SongViewerEvent.DeleteSong)
                        showDeleteDialog = false
                        onNavigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

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
                if (!scrollState.isScrollInProgress) {
                    val currentScroll = scrollState.value
                    val maxScroll = scrollState.maxValue
                    if (currentScroll < maxScroll) {
                        scrollState.animateScrollTo(
                            value = currentScroll + (state.scrollSpeed * 8f).toInt().coerceAtLeast(2),
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    } else {
                        viewModel.onEvent(SongViewerEvent.ToggleAutoScroll)
                        break
                    }
                }
                delay(16)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.song?.title ?: "Canción",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!state.song?.artist.isNullOrBlank()) {
                            Text(
                                text = state.song?.artist ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    // Notation toggle (Do / C)
                    IconButton(onClick = { viewModel.onEvent(SongViewerEvent.ToggleSpanishNotation) }) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (state.useSpanishNotation) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.useSpanishNotation) "Do" else "C",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.useSpanishNotation) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (state.song != null) {
                        IconButton(onClick = { onNavigateToEdit(state.song.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar Canción",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
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
                        .padding(horizontal = 20.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Metadatos de la Canción ──────────────────────────────
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val netShift = state.transposeSemitones - state.capo
                            val shiftedNote = ChordTransposer.shiftNote(state.song.key, netShift, state.preferFlats)
                            val displayName = ChordTransposer.getNoteDisplayName(shiftedNote, state.useSpanishNotation)

                            Column {
                                Text("Tono", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Afinación", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.song.tuning, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("Tempo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.song.bpm} BPM", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Letra y Acordes ──────────────────────────────
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            val chordColor = MaterialTheme.colorScheme.primary
                            val highlightedContent = remember(state.displayedContent, state.useSpanishNotation, chordColor) {
                                buildHighlightedLyricsAndroid(
                                    content = state.displayedContent,
                                    useSpanish = state.useSpanishNotation,
                                    chordColor = chordColor
                                )
                            }
                            if (state.displayedContent.isBlank()) {
                                Text(
                                    text = "Sin contenido (Edita la canción para añadir letra y acordes).",
                                    fontSize = state.textSizeSp.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = (state.textSizeSp * 1.5).sp
                                )
                            } else {
                                Text(
                                    text = highlightedContent,
                                    fontSize = state.textSizeSp.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = (state.textSizeSp * 1.5).sp
                                )
                            }
                        }
                    }

                    // Spacer to clear floating bottom controls (taller when setlist bar is visible)
                    Spacer(modifier = Modifier.height(if (state.currentSetlistId != null) 244.dp else 184.dp))
                }
            }

            // ── Floating Bottom Controls ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Setlist stepper
                if (state.currentSetlistId != null) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
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
                                enabled = state.previousSongId != null
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior")
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = state.setlistName ?: "Setlist",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Pista ${state.currentSongIndex + 1} de ${state.totalSongsInSet}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    state.nextSongId?.let { onNavigateToSong(it, state.currentSetlistId) }
                                },
                                enabled = state.nextSongId != null
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente")
                            }
                        }
                    }
                }

                // Controls Panel
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
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Transposition & Capo Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Tono:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.TransposeDown) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Bajar Tono", modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    text = if (state.transposeSemitones > 0) "+${state.transposeSemitones}" else "${state.transposeSemitones}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.TransposeUp) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Subir Tono", modifier = Modifier.size(18.dp))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Capo:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.CapoDown) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Bajar Capo", modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    text = "${state.capo}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.CapoUp) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Subir Capo", modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                        // Text size, AutoScroll FAB, Speed Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.DecreaseTextSize) }) {
                                    Icon(Icons.Default.TextDecrease, contentDescription = "Texto menor")
                                }
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.IncreaseTextSize) }) {
                                    Icon(Icons.Default.TextIncrease, contentDescription = "Texto mayor")
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (state.preferFlats) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { viewModel.onEvent(SongViewerEvent.ToggleFlats) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (state.preferFlats) "b" else "#",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.preferFlats) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // AutoScroll button
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { viewModel.onEvent(SongViewerEvent.ToggleAutoScroll) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (state.isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "AutoScroll",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.onEvent(SongViewerEvent.DecreaseSpeed) }) {
                                    Icon(Icons.Default.FastRewind, contentDescription = "Más lento")
                                }
                                val speedText = if (state.scrollSpeed % 1f == 0f) {
                                    "${state.scrollSpeed.toInt()}x"
                                } else {
                                    "${state.scrollSpeed}x"
                                }
                                Text(
                                    text = speedText,
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
    }
}

private fun buildHighlightedLyricsAndroid(
    content: String,
    useSpanish: Boolean,
    chordColor: Color
): AnnotatedString {
    val chordRegex = if (useSpanish) ChordTransposer.spanishChordRegex else ChordTransposer.chordRegex
    return buildAnnotatedString {
        val lines = content.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            val lineStart = length
            append(line)
            if (line.contains("[") && line.contains("]")) {
                Regex("""\[([^\]]+)\]""").findAll(line).forEach { bracketMatch ->
                    val inner = bracketMatch.groupValues[1].trim()
                    val innerTokens = inner.split(Regex("""\s+""")).filter { it.isNotEmpty() }
                    if (innerTokens.isNotEmpty() && innerTokens.all { chordRegex.matches(it) }) {
                        addStyle(
                            style = SpanStyle(color = chordColor, fontWeight = FontWeight.Bold),
                            start = lineStart + bracketMatch.range.first,
                            end = lineStart + bracketMatch.range.last + 1
                        )
                    }
                }
            } else if (ChordTransposer.isChordLine(line, toSpanish = !useSpanish)) {
                chordRegex.findAll(line).forEach { match ->
                    addStyle(
                        style = SpanStyle(color = chordColor, fontWeight = FontWeight.Bold),
                        start = lineStart + match.range.first,
                        end = lineStart + match.range.last + 1
                    )
                }
            }
            if (lineIdx < lines.lastIndex) append("\n")
        }
    }
}
