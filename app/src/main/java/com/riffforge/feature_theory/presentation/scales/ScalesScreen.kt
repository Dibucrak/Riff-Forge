package com.riffforge.feature_theory.presentation.scales

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.feature_theory.domain.util.ScaleLibrary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScalesScreen(
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: ScalesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escalas y Modos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // ── Tónica ──────────────────────────────
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
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Tónica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(ScaleLibrary.roots) { root ->
                                val useSpanish = com.riffforge.core.domain.util.AppSettings.useSpanishNotation
                                val displayRoot = com.riffforge.feature_songs.domain.util.ChordTransposer.getNoteDisplayName(root, useSpanish)
                                val isSelected = state.selectedRoot == root
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                        )
                                        .clickable { viewModel.onEvent(ScalesEvent.SelectRoot(root)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayRoot,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Tipo de Escala ──────────────────────────────
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
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Escala / Modo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val scaleListState = rememberLazyListState()
                        LaunchedEffect(state.selectedScale?.name) {
                            val idx = ScaleLibrary.scales.indexOfFirst { it.name == state.selectedScale?.name }
                            if (idx >= 0) {
                                scaleListState.animateScrollToItem(idx)
                            }
                        }

                        LazyRow(
                            state = scaleListState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(ScaleLibrary.scales) { scale ->
                                val isSelected = state.selectedScale?.name == scale.name
                                Box(
                                    modifier = Modifier
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                        )
                                        .clickable { viewModel.onEvent(ScalesEvent.SelectScale(scale)) }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = scale.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Descripción y Fórmula ──────────────────────────────
                if (state.selectedScale != null) {
                    val useSpanish = com.riffforge.core.domain.util.AppSettings.useSpanishNotation
                    val displayRoot = com.riffforge.feature_songs.domain.util.ChordTransposer.getNoteDisplayName(state.selectedRoot, useSpanish)
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
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$displayRoot ${state.selectedScale.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (state.selectedScale.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.selectedScale.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Diapasón Interactivo ──────────────────────────────
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
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mapa del Diapasón (12 Trastes)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tónica",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(SystemGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Notas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            FretboardCanvas(
                                targetNotesIndices = state.targetNotesIndices,
                                rootIndex = ScaleLibrary.roots.indexOf(state.selectedRoot),
                                useSpanish = com.riffforge.core.domain.util.AppSettings.useSpanishNotation
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FretboardCanvas(
    targetNotesIndices: List<Int>,
    rootIndex: Int,
    useSpanish: Boolean = com.riffforge.core.domain.util.AppSettings.useSpanishNotation
) {
    val rootColor = MaterialTheme.colorScheme.primary
    val onRootColor = MaterialTheme.colorScheme.onPrimary
    val noteColor = SystemGreen
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val fretMarkerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    Canvas(modifier = Modifier.size(width = 920.dp, height = 210.dp)) {
        val numStrings = 6
        val numFrets = 12

        val stringSpacing = size.height / (numStrings + 1)
        val fretSpacing = size.width / (numFrets + 1.4f)
        val tuningIndices = listOf(4, 11, 7, 2, 9, 4) // e B G D A E
        val stringNames = if (useSpanish) {
            listOf("mi", "Si", "Sol", "Re", "La", "Mi")
        } else {
            listOf("e", "B", "G", "D", "A", "E")
        }

        // Fret inlays (3, 5, 7, 9, 12)
        val markers = listOf(3, 5, 7, 9, 12)
        markers.forEach { fret ->
            val cx = (fret * fretSpacing) - (fretSpacing / 2) + fretSpacing
            val cy = size.height / 2

            if (fret == 12) {
                drawCircle(color = fretMarkerColor, radius = 4.dp.toPx(), center = Offset(cx, cy - stringSpacing * 0.8f))
                drawCircle(color = fretMarkerColor, radius = 4.dp.toPx(), center = Offset(cx, cy + stringSpacing * 0.8f))
            } else {
                drawCircle(color = fretMarkerColor, radius = 5.5.dp.toPx(), center = Offset(cx, cy))
            }
        }

        // Strings
        val labelPaint = android.graphics.Paint().apply {
            color = onSurfaceColor.toArgb()
            textSize = 11.dp.toPx()
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val openStartOffset = fretSpacing * 1.15f
        for (i in 0 until numStrings) {
            val y = stringSpacing * (i + 1)
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.4f),
                start = Offset(openStartOffset, y),
                end = Offset(size.width, y),
                strokeWidth = (1.1f + (i * 0.25f)).dp.toPx()
            )

            drawContext.canvas.nativeCanvas.drawText(
                stringNames[i],
                fretSpacing * 0.35f,
                y + 4.dp.toPx(),
                labelPaint
            )
        }

        // Frets
        val fretNumberPaint = android.graphics.Paint().apply {
            color = onSurfaceColor.copy(alpha = 0.6f).toArgb()
            textSize = 11.dp.toPx()
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        for (i in 1..numFrets + 1) {
            val x = openStartOffset + ((i - 1) * fretSpacing)
            val isNut = i == 1
            drawLine(
                color = onSurfaceColor.copy(alpha = if (isNut) 0.9f else 0.5f),
                start = Offset(x, stringSpacing),
                end = Offset(x, stringSpacing * numStrings),
                strokeWidth = if (isNut) 4.dp.toPx() else 1.4.dp.toPx()
            )

            if (i > 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${i - 1}",
                    x - (fretSpacing / 2),
                    stringSpacing / 2f,
                    fretNumberPaint
                )
            }
        }

        // Notes
        val notePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        for (string in 0 until numStrings) {
            for (fret in 0..numFrets) {
                val noteIndex = (tuningIndices[string] + fret) % 12

                if (targetNotesIndices.contains(noteIndex)) {
                    val isRoot = noteIndex == rootIndex

                    val x = if (fret == 0) {
                        openStartOffset - 12.dp.toPx()
                    } else {
                        openStartOffset + ((fret - 0.5f) * fretSpacing)
                    }
                    val y = stringSpacing * (string + 1)

                    drawCircle(
                        color = if (isRoot) rootColor else noteColor,
                        radius = 10.5.dp.toPx(),
                        center = Offset(x, y)
                    )

                    val noteName = com.riffforge.feature_songs.domain.util.ChordTransposer.getNoteDisplayName(
                        ScaleLibrary.roots[noteIndex],
                        useSpanish
                    )
                    notePaint.color = if (isRoot) onRootColor.toArgb() else android.graphics.Color.BLACK
                    notePaint.textSize = if (noteName.length > 2) 8.5.dp.toPx() else 10.dp.toPx()
                    drawContext.canvas.nativeCanvas.drawText(
                        noteName,
                        x,
                        y + 3.5.dp.toPx(),
                        notePaint
                    )
                }
            }
        }
    }
}
