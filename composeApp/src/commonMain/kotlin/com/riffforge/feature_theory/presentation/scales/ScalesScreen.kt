package com.riffforge.feature_theory.presentation.scales

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riffforge.core.audio.AudioSynth
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.shared.domain.util.ScaleLibrary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScalesScreen(
    onNavigateUp: () -> Unit = {}
) {
    val useSpanish = AppSettings.useSpanishNotation
    var selectedRoot by rememberSaveable { mutableStateOf("A") }
    var selectedScaleName by rememberSaveable { mutableStateOf("Pentatónica Menor") }

    val currentScale = remember(selectedScaleName) {
        ScaleLibrary.scales.find { it.name == selectedScaleName } ?: ScaleLibrary.scales.first()
    }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var playbackJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val rootMidiMap = mapOf(
        "C" to 48, "C#" to 49, "D" to 50, "D#" to 51,
        "E" to 52, "F" to 53, "F#" to 54, "G" to 55,
        "G#" to 56, "A" to 57, "A#" to 58, "B" to 59
    )

    val greenColor = SystemGreen

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escalas y Modos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // ── Root Selector Card ──────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tónica",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ScaleLibrary.roots) { root ->
                                val isSelected = root == selectedRoot
                                val displayRoot = NotationConverter.formatChord(root, useSpanish)
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { selectedRoot = root },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayRoot,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Scale Type Selector Card ────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Escala / Modo",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val scaleListState = rememberLazyListState()
                        LaunchedEffect(selectedScaleName) {
                            val idx = ScaleLibrary.scales.indexOfFirst { it.name == selectedScaleName }
                            if (idx >= 0) {
                                scaleListState.animateScrollToItem(idx)
                            }
                        }
                        LazyRow(
                            state = scaleListState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ScaleLibrary.scales) { scale ->
                                val isSelected = scale.name == selectedScaleName
                                Box(
                                    modifier = Modifier
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { selectedScaleName = scale.name }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = scale.name,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Scale Info Card ─────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            val displayRoot = NotationConverter.formatChord(selectedRoot, useSpanish)
                            Text(
                                text = "$displayRoot ${currentScale.name}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentScale.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = {
                                playbackJob?.cancel()
                                playbackJob = scope.launch {
                                    val baseMidi = rootMidiMap[selectedRoot] ?: 57
                                    for (interval in currentScale.intervals) {
                                        AudioSynth.playMidiNote(baseMidi + interval, 220)
                                        delay(200)
                                    }
                                    AudioSynth.playMidiNote(baseMidi + 12, 320)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Escuchar Escala",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Fretboard Map Card ──────────────────────────────
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Mapa del Diapasón (12 Trastes)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tónica", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)

                            Spacer(modifier = Modifier.width(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(greenColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nota de la Escala", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        FretboardCanvas(
                            rootNote = selectedRoot,
                            intervals = currentScale.intervals,
                            useSpanish = useSpanish,
                            scaleNoteColor = greenColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private val chromaticNotes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

private fun getNoteAtFret(openNote: String, fret: Int): String {
    val baseIndex = chromaticNotes.indexOf(openNote)
    if (baseIndex == -1) return openNote
    return chromaticNotes[(baseIndex + fret) % 12]
}

private fun isNoteInScale(note: String, root: String, intervals: List<Int>): Boolean {
    val rootIndex = chromaticNotes.indexOf(root)
    val noteIndex = chromaticNotes.indexOf(note)
    if (rootIndex == -1 || noteIndex == -1) return false
    val intervalFromRoot = (noteIndex - rootIndex + 12) % 12
    return intervals.contains(intervalFromRoot)
}

@Composable
private fun FretboardCanvas(
    rootNote: String,
    intervals: List<Int>,
    useSpanish: Boolean,
    scaleNoteColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val nutColor = MaterialTheme.colorScheme.onSurface
    val rootColor = MaterialTheme.colorScheme.primary
    val inlayColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

    // Standard tuning open strings from high E (top line) to low E (bottom line)
    val openStrings = listOf("E", "B", "G", "D", "A", "E")
    val stringLabels = if (useSpanish) listOf("mi", "Si", "Sol", "Re", "La", "Mi") else openStrings

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // String names column on the left
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            stringLabels.forEach { stringNote ->
                Text(
                    text = stringNote,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val numStrings = 6
                val numFrets = 12
                val width = size.width
                val height = size.height
                val stringSpacing = height / (numStrings - 1)
                val fretSpacing = width / numFrets

                // Nut
                drawLine(
                    color = nutColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, height),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Frets
                for (fret in 1..numFrets) {
                    val x = fret * fretSpacing
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )

                    if (fret in listOf(3, 5, 7, 9)) {
                        drawCircle(
                            color = inlayColor,
                            radius = 6.dp.toPx(),
                            center = Offset(x - fretSpacing / 2f, height / 2f)
                        )
                    } else if (fret == 12) {
                        drawCircle(
                            color = inlayColor,
                            radius = 5.dp.toPx(),
                            center = Offset(x - fretSpacing / 2f, height * 0.25f)
                        )
                        drawCircle(
                            color = inlayColor,
                            radius = 5.dp.toPx(),
                            center = Offset(x - fretSpacing / 2f, height * 0.75f)
                        )
                    }
                }

                // Strings
                for (stringIdx in 0 until numStrings) {
                    val y = stringIdx * stringSpacing
                    val strokeW = (0.9f + stringIdx * 0.22f).dp.toPx()
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = strokeW
                    )
                }

                // Scale notes
                openStrings.forEachIndexed { stringIdx, openNote ->
                    val y = stringIdx * stringSpacing
                    for (fret in 0..numFrets) {
                        val noteAtFret = getNoteAtFret(openNote, fret)
                        val inScale = isNoteInScale(noteAtFret, rootNote, intervals)

                        if (inScale) {
                            val isRoot = noteAtFret == rootNote
                            val x = if (fret == 0) 0f else (fret - 0.5f) * fretSpacing
                            val radius = if (isRoot) 7.dp.toPx() else 5.5.dp.toPx()

                            drawCircle(
                                color = if (isRoot) rootColor else scaleNoteColor,
                                radius = radius,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }
    }
}
