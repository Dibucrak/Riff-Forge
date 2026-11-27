package com.riffforge.feature_theory.presentation.progressions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.feature_ear_training.domain.util.AudioSynth as EarAudioSynth
import com.riffforge.shared.domain.model.Progression
import com.riffforge.shared.domain.util.ProgressionLibrary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ALL_KEYS = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
private val SHARP_SCALE = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
private val FLAT_SCALE = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

private fun noteToIndex(note: String): Int {
    val clean = note.trim()
    val sharpIdx = SHARP_SCALE.indexOf(clean)
    if (sharpIdx >= 0) return sharpIdx
    val flatIdx = FLAT_SCALE.indexOf(clean)
    return if (flatIdx >= 0) flatIdx else 0
}

private fun extractRootAndSuffix(chord: String): Pair<String, String> {
    if (chord.length >= 2 && (chord[1] == '#' || chord[1] == 'b')) {
        return chord.substring(0, 2) to chord.substring(2)
    }
    return chord.take(1) to chord.drop(1)
}

private fun transposeSingleChord(chord: String, semitones: Int, preferFlats: Boolean, useSpanish: Boolean): String {
    val (root, suffix) = extractRootAndSuffix(chord)
    val newIdx = (noteToIndex(root) + semitones % 12 + 12) % 12
    val targetNote = if (preferFlats) FLAT_SCALE[newIdx] else SHARP_SCALE[newIdx]
    val displayRoot = NotationConverter.formatChord(targetNote, useSpanish)
    return "$displayRoot$suffix"
}

private fun baseTonicForProgression(p: Progression): String {
    val degrees = p.romanNumerals.split("-").map { it.trim() }
    val tonicIdx = degrees.indexOfFirst { d ->
        d.equals("I", ignoreCase = true) ||
            d.startsWith("I7", ignoreCase = true) ||
            d.startsWith("Imaj", ignoreCase = true) ||
            d == "i"
    }
    val refChord = p.chords.getOrNull(if (tonicIdx >= 0) tonicIdx else 0) ?: "C"
    return extractRootAndSuffix(refChord).first
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressionScreen(
    onNavigateUp: () -> Unit = {}
) {
    val useSpanish = AppSettings.useSpanishNotation
    var selectedGenre by remember { mutableStateOf(ProgressionLibrary.genres.first()) }
    var selectedKey by remember { mutableStateOf("C") }
    var keyMenuExpanded by remember { mutableStateOf(false) }
    var genreMenuExpanded by remember { mutableStateOf(false) }

    val displayedProgressions = remember(selectedGenre) {
        ProgressionLibrary.progressions.filter { it.genre == selectedGenre }
    }
    var featuredProgressionId by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var playbackJob by remember { mutableStateOf<Job?>(null) }

    val preferFlats = selectedKey in listOf("F", "Bb", "Eb", "Ab")
    val featuredProgression = displayedProgressions.find { it.id == featuredProgressionId }
        ?: displayedProgressions.firstOrNull()
    val otherProgressions = ProgressionLibrary.progressions.filter { it.id != featuredProgression?.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progresiones Armónicas",
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Dual Dropdowns: Tono + Género ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    val keyDisplay = NotationConverter.formatChord(selectedKey, useSpanish)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { keyMenuExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tono: $keyDisplay",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Seleccionar tono",
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
                        ALL_KEYS.forEach { key ->
                            val label = NotationConverter.formatChord(key, useSpanish)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "$label Mayor / menor",
                                        fontWeight = if (key == selectedKey) FontWeight.Bold else FontWeight.Normal,
                                        color = if (key == selectedKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedKey = key
                                    keyMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { genreMenuExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedGenre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Seleccionar género",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = genreMenuExpanded,
                        onDismissRequest = { genreMenuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        ProgressionLibrary.genres.forEach { genre ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = genre,
                                        fontWeight = if (genre == selectedGenre) FontWeight.Bold else FontWeight.Normal,
                                        color = if (genre == selectedGenre) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedGenre = genre
                                    genreMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Featured Progression Card ────────────────────────────────
            if (featuredProgression != null) {
                val baseTonic = baseTonicForProgression(featuredProgression)
                val shift = (noteToIndex(selectedKey) - noteToIndex(baseTonic) + 12) % 12
                val romanDegrees = featuredProgression.romanNumerals.split("-").map { it.trim() }
                val transposedChords = featuredProgression.chords.mapIndexed { idx, chord ->
                    val degree = romanDegrees.getOrNull(idx).orEmpty()
                    transposeSingleChord(chord, shift, preferFlats || degree.contains("b"), useSpanish)
                }

                val americanChordsForAudio = featuredProgression.chords.mapIndexed { idx, chord ->
                    val degree = romanDegrees.getOrNull(idx).orEmpty()
                    transposeSingleChord(chord, shift, preferFlats || degree.contains("b"), false)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = featuredProgression.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val barCount = if (featuredProgression.id.contains("12", ignoreCase = true) || featuredProgression.name.contains("12")) 12 else transposedChords.size
                                Text(
                                    text = "${featuredProgression.romanNumerals} · $barCount compases",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        playbackJob?.cancel()
                                        playbackJob = scope.launch {
                                            for (ch in americanChordsForAudio) {
                                                EarAudioSynth.playChordByName(ch)
                                                delay(720)
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Escuchar progresión",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = featuredProgression.genre,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            transposedChords.forEachIndexed { idx, chordName ->
                                val degreeLabel = romanDegrees.getOrNull(idx) ?: ""
                                val isPrimaryTile = idx == 0
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isPrimaryTile) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.primaryContainer
                                        )
                                        .padding(vertical = 16.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = chordName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isPrimaryTile) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (degreeLabel.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = degreeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isPrimaryTile) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (featuredProgression.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = featuredProgression.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Otras Progresiones ───────────────────────────────────────
            Text(
                text = "Otras Progresiones",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
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
                    otherProgressions.forEachIndexed { idx, prog ->
                        val baseTonic = baseTonicForProgression(prog)
                        val shift = (noteToIndex(selectedKey) - noteToIndex(baseTonic) + 12) % 12
                        val progDegrees = prog.romanNumerals.split("-").map { it.trim() }
                        val previewChords = prog.chords.mapIndexed { cIdx, chord ->
                            val deg = progDegrees.getOrNull(cIdx).orEmpty()
                            transposeSingleChord(chord, shift, preferFlats || deg.contains("b"), useSpanish)
                        }.joinToString(" – ")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedGenre = prog.genre
                                    featuredProgressionId = prog.id
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prog.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$previewChords  ·  ${prog.genre}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        if (idx < otherProgressions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
