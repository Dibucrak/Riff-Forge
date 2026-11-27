package com.riffforge.feature_chords.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.unit.sp
import com.riffforge.core.audio.AudioSynth
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.shared.domain.model.Chord
import com.riffforge.shared.domain.util.ChordLibrary

private data class ChordVoicingOption(
    val label: String,
    val chord: Chord
)

private val CHROMATIC_NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

private fun computeChordNotes(root: String, type: String, useSpanish: Boolean): String {
    val rootIdx = CHROMATIC_NOTES.indexOf(root).coerceAtLeast(0)
    val intervals = when (type) {
        "Mayor", "Mayor (Cejilla)" -> listOf(0, 4, 7)
        "Menor" -> listOf(0, 3, 7)
        "7" -> listOf(0, 4, 7, 10)
        "Maj7" -> listOf(0, 4, 7, 11)
        "Min7" -> listOf(0, 3, 7, 10)
        "Sus4" -> listOf(0, 5, 7)
        "Sus2" -> listOf(0, 2, 7)
        "Dim" -> listOf(0, 3, 6)
        "Aug" -> listOf(0, 4, 8)
        else -> listOf(0, 4, 7)
    }
    return intervals.joinToString(" · ") { step ->
        val note = CHROMATIC_NOTES[(rootIdx + step) % 12]
        NotationConverter.formatChord(note, useSpanish)
    }
}

private fun buildVoicingOptions(root: String, type: String, baseChord: Chord?): List<ChordVoicingOption> {
    val list = mutableListOf<ChordVoicingOption>()
    val rootIdx = CHROMATIC_NOTES.indexOf(root).coerceAtLeast(0)

    if (baseChord != null) {
        val baseLabel = if (!baseChord.isBarre && baseChord.startingFret == 1) {
            "Abierto"
        } else {
            "Cejilla ${baseChord.startingFret}"
        }
        list.add(ChordVoicingOption(baseLabel, baseChord))
    }

    val fretA = ((rootIdx - 9 + 12) % 12).let { if (it == 0) 12 else it }
    val aShape = when (type) {
        "Mayor" -> Chord("${root}_a_maj", root, type, listOf(-1, fretA, fretA + 2, fretA + 2, fretA + 2, fretA), listOf(0, 1, 2, 3, 4, 1), "Media", true, fretA, fretA)
        "Menor" -> Chord("${root}_a_min", root, type, listOf(-1, fretA, fretA + 2, fretA + 2, fretA + 1, fretA), listOf(0, 1, 3, 4, 2, 1), "Media", true, fretA, fretA)
        "7" -> Chord("${root}_a_7", root, type, listOf(-1, fretA, fretA + 2, fretA, fretA + 2, fretA), listOf(0, 1, 3, 1, 4, 1), "Media", true, fretA, fretA)
        "Maj7" -> Chord("${root}_a_maj7", root, type, listOf(-1, fretA, fretA + 2, fretA + 1, fretA + 2, fretA), listOf(0, 1, 3, 2, 4, 1), "Media", true, fretA, fretA)
        "Min7" -> Chord("${root}_a_min7", root, type, listOf(-1, fretA, fretA + 2, fretA, fretA + 1, fretA), listOf(0, 1, 3, 1, 2, 1), "Media", true, fretA, fretA)
        "Sus4" -> Chord("${root}_a_sus4", root, type, listOf(-1, fretA, fretA + 2, fretA + 2, fretA + 3, fretA), listOf(0, 1, 2, 3, 4, 1), "Media", true, fretA, fretA)
        else -> null
    }
    if (aShape != null && list.none { it.chord.stringFrets == aShape.stringFrets }) {
        list.add(ChordVoicingOption("Cejilla $fretA", aShape))
    }

    val fretE = ((rootIdx - 4 + 12) % 12).let { if (it == 0) 12 else it }
    val eShape = when (type) {
        "Mayor" -> Chord("${root}_e_maj", root, type, listOf(fretE, fretE + 2, fretE + 2, fretE + 1, fretE, fretE), listOf(1, 3, 4, 2, 1, 1), "Media", true, fretE, fretE)
        "Menor" -> Chord("${root}_e_min", root, type, listOf(fretE, fretE + 2, fretE + 2, fretE, fretE, fretE), listOf(1, 3, 4, 1, 1, 1), "Media", true, fretE, fretE)
        "7" -> Chord("${root}_e_7", root, type, listOf(fretE, fretE + 2, fretE, fretE + 1, fretE, fretE), listOf(1, 3, 1, 2, 1, 1), "Media", true, fretE, fretE)
        "Maj7" -> Chord("${root}_e_maj7", root, type, listOf(fretE, -1, fretE + 1, fretE + 1, fretE, -1), listOf(1, 0, 3, 4, 2, 0), "Media", false, 0, fretE)
        "Min7" -> Chord("${root}_e_min7", root, type, listOf(fretE, fretE + 2, fretE, fretE, fretE, fretE), listOf(1, 3, 1, 1, 1, 1), "Media", true, fretE, fretE)
        "Sus4" -> Chord("${root}_e_sus4", root, type, listOf(fretE, fretE + 2, fretE + 2, fretE + 2, fretE, fretE), listOf(1, 2, 3, 4, 1, 1), "Media", true, fretE, fretE)
        else -> null
    }
    if (eShape != null && list.none { it.chord.stringFrets == eShape.stringFrets }) {
        list.add(ChordVoicingOption("Cejilla $fretE", eShape))
    }

    return list.sortedBy { if (it.label == "Abierto") 0 else it.chord.startingFret }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordDictionaryScreen(
    onNavigateUp: () -> Unit = {}
) {
    val useSpanish = AppSettings.useSpanishNotation
    var selectedRoot by rememberSaveable { mutableStateOf("C") }
    var selectedType by rememberSaveable { mutableStateOf("Mayor") }

    val displayTypes = remember {
        ChordLibrary.types.filter { it != "Mayor (Cejilla)" }
    }

    val baseChord = remember(selectedRoot, selectedType) {
        ChordLibrary.getChord(selectedRoot, selectedType)
    }
    val voicingOptions = remember(selectedRoot, selectedType, baseChord) {
        buildVoicingOptions(selectedRoot, selectedType, baseChord)
    }
    var selectedVoicingIndex by remember(selectedRoot, selectedType) { mutableStateOf(0) }
    val activeChord = voicingOptions.getOrNull(selectedVoicingIndex)?.chord ?: baseChord

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diccionario de Acordes", fontWeight = FontWeight.ExtraBold) },
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── 7 Root Notes Row ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChordLibrary.roots.forEach { root ->
                    val displayRoot = NotationConverter.formatChord(root, useSpanish)
                    val isSelected = selectedRoot == root
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedRoot = root },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayRoot,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Segmented Quality Bar ────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    displayTypes.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedType = type }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Main Chord Card ──────────────────────────────────────────
            val spanishRoot = NotationConverter.formatChord(selectedRoot, true)
            val titleText = if (useSpanish) {
                "$spanishRoot $selectedType ($selectedRoot)"
            } else {
                "$selectedRoot $selectedType ($spanishRoot)"
            }
            val notesText = remember(selectedRoot, selectedType, useSpanish) {
                computeChordNotes(selectedRoot, selectedType, useSpanish)
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Notas: $notesText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (activeChord != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        AudioSynth.playChord(
                                            frets = activeChord.stringFrets,
                                            baseFret = activeChord.startingFret.coerceAtLeast(1)
                                        )
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Escuchar acorde",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "Traste ${activeChord.startingFret}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        if (activeChord != null) {
                            ChordFretboardDiagram(chord = activeChord)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Diagrama no disponible",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (voicingOptions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            voicingOptions.forEachIndexed { idx, option ->
                                val isCurrentPos = idx == selectedVoicingIndex
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedVoicingIndex = idx },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isCurrentPos) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface,
                                    border = if (isCurrentPos) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrentPos) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ChordDiagramCanvas(chord: Chord) = ChordFretboardDiagram(chord)

@Composable
fun ChordFretboardDiagram(chord: Chord) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Open / Muted string markers row
        Row(
            modifier = Modifier.width(200.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            chord.stringFrets.forEach { fret ->
                Text(
                    text = when (fret) {
                        -1 -> "✕"
                        0 -> "○"
                        else -> " "
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (fret) {
                        0 -> SystemGreen
                        else -> mutedColor
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.size(width = 200.dp, height = 210.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val numStrings = 6
                val numFrets = 5
                val stringSpacing = size.width / (numStrings - 1)
                val fretSpacing = size.height / numFrets

                for (i in 0..numFrets) {
                    val y = i * fretSpacing
                    val isNut = i == 0 && chord.startingFret == 1
                    drawLine(
                        color = if (isNut) onSurfaceColor else onSurfaceColor.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = if (isNut) 3.5.dp.toPx() else 1.2.dp.toPx()
                    )
                }

                for (i in 0 until numStrings) {
                    val x = i * stringSpacing
                    drawLine(
                        color = onSurfaceColor.copy(alpha = 0.35f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }

                val barreGroup = chord.fingers.zip(chord.stringFrets)
                    .mapIndexed { idx, p -> Triple(idx, p.first, p.second) }
                    .filter { it.second == 1 && it.third == chord.startingFret && chord.isBarre }

                if (barreGroup.size > 1) {
                    val startX = barreGroup.minOf { it.first } * stringSpacing
                    val endX = barreGroup.maxOf { it.first } * stringSpacing
                    val y = fretSpacing / 2f
                    drawLine(
                        color = primaryColor,
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 17.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                chord.stringFrets.forEachIndexed { idx, fretVal ->
                    if (fretVal > 0) {
                        val visualFret = fretVal - chord.startingFret + 1
                        if (visualFret in 1..numFrets) {
                            val cx = idx * stringSpacing
                            val cy = ((visualFret - 1) * fretSpacing) + (fretSpacing / 2f)
                            drawCircle(
                                color = primaryColor,
                                radius = 9.5.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }
            }

            // Finger number overlays in Compose (100% multiplatform without nativeCanvas)
            val numStrings = 6
            val numFrets = 5
            chord.stringFrets.forEachIndexed { idx, fretVal ->
                val finger = chord.fingers.getOrNull(idx) ?: 0
                if (fretVal > 0 && finger > 0) {
                    val visualFret = fretVal - chord.startingFret + 1
                    if (visualFret in 1..numFrets) {
                        val xDp = (idx * (200f / (numStrings - 1))).dp - 9.dp
                        val yDp = (((visualFret - 1) * (210f / numFrets)) + (210f / numFrets / 2f)).dp - 9.dp
                        Box(
                            modifier = Modifier
                                .offset(x = xDp, y = yDp)
                                .size(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = finger.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = onPrimaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}
