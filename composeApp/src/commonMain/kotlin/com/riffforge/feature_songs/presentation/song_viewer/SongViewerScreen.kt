package com.riffforge.feature_songs.presentation.song_viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riffforge.core.domain.store.RepertoireStore
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.ChordTransposer
import com.riffforge.core.domain.util.NotationConverter
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongViewerScreen(
    songId: String? = null,
    setlistId: String? = null,
    onNavigateUp: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onNavigateToSong: (songId: String, setlistId: String) -> Unit = { _, _ -> }
) {
    val useSpanish = AppSettings.useSpanishNotation
    val song = RepertoireStore.getSong(songId)
    val setlist = if (setlistId != null) RepertoireStore.getSetlist(setlistId) else null
    val setlistSongs = remember(setlist?.songIds?.toList(), RepertoireStore.songs.toList()) {
        if (setlist != null) RepertoireStore.getSongsForSetlist(setlist.id) else emptyList()
    }
    val currentSetlistIndex = remember(song?.id, setlistSongs) {
        setlistSongs.indexOfFirst { it.id == song?.id }
    }

    var transposeSemitones by remember(song?.id) { mutableStateOf(0) }
    var capoFret by remember(song?.id) { mutableStateOf(0) }
    var fontSizeSp by remember { mutableStateOf(16f) }
    var isAutoScrolling by remember { mutableStateOf(false) }
    val speedSteps = remember { listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 2.5f, 3.0f) }
    var speedStepIndex by remember { mutableStateOf(2) } // 1.0x default
    val scrollSpeed = speedSteps[speedStepIndex]
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(isAutoScrolling, scrollSpeed) {
        while (isAutoScrolling) {
            if (!scrollState.isScrollInProgress) {
                scrollState.scrollBy(scrollSpeed * 1.4f)
                if (scrollState.value >= scrollState.maxValue && scrollState.maxValue > 0) {
                    isAutoScrolling = false
                }
            }
            delay(32)
        }
    }

    val netSemitones = transposeSemitones - capoFret

    val effectiveKey = remember(song?.key, netSemitones, useSpanish) {
        val rawKey = song?.key ?: "C"
        val transposedKey = ChordTransposer.transposeText("[$rawKey]", netSemitones)
            .removePrefix("[")
            .removeSuffix("]")
        NotationConverter.formatChord(transposedKey, useSpanish)
    }

    val formattedContent = remember(song?.lyricsAndChords, netSemitones, useSpanish) {
        val rawText = song?.lyricsAndChords ?: ""
        val transposed = ChordTransposer.transposeText(rawText, netSemitones)
        val stripped = transposed.replace(Regex("\\[(.*?)\\]")) { matchResult ->
            matchResult.groupValues[1]
        }
        NotationConverter.convertLyricsText(stripped, useSpanish)
    }

    val chordHighlightColor = MaterialTheme.colorScheme.primary
    val highlightedContent = remember(formattedContent, useSpanish, chordHighlightColor) {
        buildHighlightedLyrics(formattedContent, useSpanish, chordHighlightColor)
    }

    if (showDeleteDialog && song != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar canción local?", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará \"${song.title}\" de tu dispositivo.") },
            confirmButton = {
                Button(
                    onClick = {
                        RepertoireStore.deleteSong(song.id)
                        showDeleteDialog = false
                        onNavigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = song?.title ?: "Canción no encontrada",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (song != null) {
                            Text(
                                text = song.artist,
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { AppSettings.toggleSpanishNotation() }) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (useSpanish) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (useSpanish) "Do" else "C",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (useSpanish) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (song != null) {
                        IconButton(onClick = { onEditClick(song.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Canción",
                                tint = MaterialTheme.colorScheme.primary
                            )
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
            song?.let { currentSong ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // ── Song Metadata Row ──────────────────────────
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tono",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = effectiveKey,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = "Afinación",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currentSong.tuning,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column {
                                Text(
                                    text = "Tempo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${currentSong.bpm} BPM",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // ── Setlist Stepper Bar ──────────────────────────
                    if (setlist != null && setlistSongs.size > 1 && currentSetlistIndex >= 0) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentSetlistIndex > 0) {
                                            onNavigateToSong(
                                                setlistSongs[currentSetlistIndex - 1].id,
                                                setlist.id
                                            )
                                        }
                                    },
                                    enabled = currentSetlistIndex > 0
                                ) {
                                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Canción Anterior")
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LibraryMusic,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = setlist.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Pista ${currentSetlistIndex + 1} de ${setlistSongs.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (currentSetlistIndex < setlistSongs.lastIndex) {
                                            onNavigateToSong(
                                                setlistSongs[currentSetlistIndex + 1].id,
                                                setlist.id
                                            )
                                        }
                                    },
                                    enabled = currentSetlistIndex < setlistSongs.lastIndex
                                ) {
                                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Siguiente Canción")
                                }
                            }
                        }
                    }

                    // ── Lyrics & Chords Canvas ──────────────────────────
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                if (formattedContent.isBlank()) {
                                    Text(
                                        text = "Sin contenido (Edita la canción para añadir letra y acordes).",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = fontSizeSp.sp,
                                        lineHeight = (fontSizeSp * 1.5f).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = highlightedContent,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = fontSizeSp.sp,
                                        lineHeight = (fontSizeSp * 1.5f).sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // ── Bottom Control Dock ──────────────────────────
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            // Row 1: Transpose & Capo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tono:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = { if (transposeSemitones > -12) transposeSemitones-- },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Bajar Semitono", modifier = Modifier.size(18.dp))
                                    }
                                    Text(
                                        text = if (transposeSemitones > 0) "+$transposeSemitones" else "$transposeSemitones",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { if (transposeSemitones < 12) transposeSemitones++ },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Subir Semitono", modifier = Modifier.size(18.dp))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Capo:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = { if (capoFret > 0) capoFret-- },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Menos Capo", modifier = Modifier.size(18.dp))
                                    }
                                    Text(
                                        text = "$capoFret",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { if (capoFret < 12) capoFret++ },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Más Capo", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Row 2: Font Size & Teleprompter Auto-Scroll
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (fontSizeSp > 12f) fontSizeSp -= 2f },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.TextDecrease, contentDescription = "Reducir Letra")
                                    }
                                    IconButton(
                                        onClick = { if (fontSizeSp < 28f) fontSizeSp += 2f },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.TextIncrease, contentDescription = "Aumentar Letra")
                                    }
                                }

                                // Play/Pause Button
                                IconButton(
                                    onClick = { isAutoScrolling = !isAutoScrolling },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Auto-Scroll",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                // Speed Controls
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (speedStepIndex > 0) speedStepIndex-- },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.FastRewind, contentDescription = "Más Lento")
                                    }
                                    val speedLabel = if (scrollSpeed % 1f == 0f) "${scrollSpeed.toInt()}x" else "${scrollSpeed}x"
                                    Text(
                                        text = speedLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { if (speedStepIndex < speedSteps.lastIndex) speedStepIndex++ },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.FastForward, contentDescription = "Más Rápido")
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Text(
                    text = "Canción no encontrada.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

private fun buildHighlightedLyrics(
    content: String,
    useSpanish: Boolean,
    chordColor: Color
): AnnotatedString {
    val chordRegex = if (useSpanish) {
        Regex("""(?<![A-Za-z])(Sol|Do|Re|Mi|Fa|La|Si)(#|b)?(m7b5|maj7|min7|dim7|aug7|sus2|sus4|add9|maj|min|dim|aug|sus|m)?(\d+)?(?:/(Sol|Do|Re|Mi|Fa|La|Si)(#|b)?)?(?![A-Za-z0-9#b])""")
    } else {
        Regex("""(?<![A-Za-z])([A-G](?:#|b)?)(m7b5|maj7|min7|dim7|aug7|sus2|sus4|add9|maj|min|dim|aug|sus|m)?(\d+)?(?:/([A-G](?:#|b)?))?(?![A-Za-z0-9#b])""")
    }

    return buildAnnotatedString {
        val lines = content.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            val lineStart = length
            append(line)
            val words = line.trim().split(Regex("""\s+""")).filter { it.isNotEmpty() }
            val chordLineRatio = if (words.isNotEmpty()) {
                words.count { chordRegex.matches(it) }.toFloat() / words.size
            } else 0f

            if (chordLineRatio >= 0.45f) {
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
