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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.feature_chords.domain.model.Chord
import com.riffforge.feature_chords.domain.util.ChordLibrary
import com.riffforge.feature_songs.domain.util.ChordTransposer

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
        ChordTransposer.getNoteDisplayName(note, useSpanish)
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

    // A-shape barre on 5th string (open A = 9)
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

    // E-shape barre on 6th string (open E = 4)
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
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: ChordDictionaryViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val useSpanish = AppSettings.useSpanishNotation

    // Clean quality list (Cejilla variants now live in the Position selector below!)
    val displayTypes = remember {
        ChordLibrary.types.filter { it != "Mayor (Cejilla)" }
    }

    val voicingOptions = remember(state.selectedRoot, state.selectedType, state.currentChord) {
        buildVoicingOptions(state.selectedRoot, state.selectedType, state.currentChord)
    }
    var selectedVoicingIndex by remember(state.selectedRoot, state.selectedType) { mutableIntStateOf(0) }
    val activeVoicing = voicingOptions.getOrNull(selectedVoicingIndex) ?: voicingOptions.firstOrNull()
    val activeChord = activeVoicing?.chord ?: state.currentChord

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── 7 Root Notes Row (C D E F G A B) ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChordLibrary.roots.forEach { root ->
                    val displayRoot = ChordTransposer.getNoteDisplayName(root, useSpanish)
                    val isSelected = state.selectedRoot == root
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.onEvent(ChordDictionaryEvent.SelectRoot(root)) },
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

            // ── Segmented Quality Bar (Mayor | Menor | 7 | Maj7 ...) ─────
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
                        val isSelected = state.selectedType == type
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.onEvent(ChordDictionaryEvent.SelectType(type)) }
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

            // ── Main Chord Diagram & Variants Card ───────────────────────
            val americanRoot = state.selectedRoot
            val spanishRoot = ChordTransposer.getNoteDisplayName(americanRoot, true)
            val titleText = if (useSpanish) {
                "$spanishRoot ${state.selectedType} ($americanRoot)"
            } else {
                "$americanRoot ${state.selectedType} ($spanishRoot)"
            }
            val notesText = remember(state.selectedRoot, state.selectedType, useSpanish) {
                computeChordNotes(state.selectedRoot, state.selectedType, useSpanish)
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
                    // Header: Title + Notes + Fret Badge
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Crisp White/Surface Inner Diagram Container
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        if (activeChord != null) {
                            ChordDiagramCanvas(chord = activeChord)
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

                    // Position Variants Row (Abierto | Cejilla 3 | Cejilla 8)
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
fun ChordDiagramCanvas(chord: Chord) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val openGreenColor = SystemGreen

    val onPrimaryArgb = onPrimaryColor.toArgb()
    val onSurfaceArgb = onSurfaceColor.toArgb()
    val mutedArgb = mutedColor.toArgb()
    val openGreenArgb = openGreenColor.toArgb()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 200.dp, height = 250.dp)) {
            val numStrings = 6
            val numFretsToShow = 5

            val stringSpacing = size.width / (numStrings - 1)
            val fretSpacing = (size.height - 36.dp.toPx()) / numFretsToShow
            val startY = 36.dp.toPx()

            // Frets
            for (i in 0..numFretsToShow) {
                val y = startY + (i * fretSpacing)
                val isNut = i == 0 && chord.startingFret == 1
                drawLine(
                    color = if (isNut) onSurfaceColor else onSurfaceColor.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isNut) 4.dp.toPx() else 1.2.dp.toPx()
                )
            }

            // Strings
            for (i in 0 until numStrings) {
                val x = i * stringSpacing
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.35f),
                    start = Offset(x, startY),
                    end = Offset(x, startY + (numFretsToShow * fretSpacing)),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // Starting fret label
            if (chord.startingFret > 1) {
                val fretLabelPaint = android.graphics.Paint().apply {
                    color = onSurfaceArgb
                    textSize = 12.dp.toPx()
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "${chord.startingFret}fr",
                    -22.dp.toPx(),
                    startY + (fretSpacing / 2) + 4.dp.toPx(),
                    fretLabelPaint
                )
            }

            // Open (Green O) / Muted (X) markers
            val markerPaint = android.graphics.Paint().apply {
                textSize = 14.dp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            chord.frets.forEachIndexed { index, fretVal ->
                val x = index * stringSpacing
                drawContext.canvas.nativeCanvas.apply {
                    if (fretVal == -1) {
                        markerPaint.color = mutedArgb
                        drawText("✕", x, startY - 8.dp.toPx(), markerPaint)
                    } else if (fretVal == 0) {
                        markerPaint.color = openGreenArgb
                        drawText("○", x, startY - 8.dp.toPx(), markerPaint)
                    }
                }
            }

            // Barre line (handles barreFret even if barreFret != startingFret)
            val barreFretTarget = if (chord.barreFret > 0) chord.barreFret else chord.startingFret
            val fretGroups = chord.fingers.zip(chord.frets)
                .mapIndexed { i, pair -> Triple(i, pair.first, pair.second) }
                .filter { it.second == 1 && it.third == barreFretTarget && chord.isBarre }

            if (fretGroups.size > 1) {
                val visualFret = barreFretTarget - chord.startingFret + 1
                if (visualFret in 1..numFretsToShow) {
                    val firstString = fretGroups.minOf { it.first }
                    val lastString = fretGroups.maxOf { it.first }
                    val startX = firstString * stringSpacing
                    val endX = lastString * stringSpacing
                    val y = startY + ((visualFret - 1) * fretSpacing) + (fretSpacing / 2)

                    drawLine(
                        color = primaryColor,
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 18.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        "1",
                        startX + ((endX - startX) / 2),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = onPrimaryArgb
                            textSize = 11.dp.toPx()
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                    )
                }
            }

            // Finger dots
            val fingerTextPaint = android.graphics.Paint().apply {
                color = onPrimaryArgb
                textSize = 11.dp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            chord.frets.forEachIndexed { index, fretVal ->
                if (fretVal > 0) {
                    val fingerNum = chord.fingers.getOrNull(index) ?: 0
                    val isPartOfBarre = chord.isBarre && fingerNum == 1 && fretVal == barreFretTarget && fretGroups.size > 1
                    if (!isPartOfBarre) {
                        val visualFret = fretVal - chord.startingFret + 1
                        if (visualFret in 1..numFretsToShow) {
                            val cx = index * stringSpacing
                            val cy = startY + ((visualFret - 1) * fretSpacing) + (fretSpacing / 2)

                            drawCircle(
                                color = primaryColor,
                                radius = 9.dp.toPx(),
                                center = Offset(cx, cy)
                            )

                            if (fingerNum > 0) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    fingerNum.toString(),
                                    cx,
                                    cy + 4.dp.toPx(),
                                    fingerTextPaint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
