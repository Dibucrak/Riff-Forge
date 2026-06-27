package com.riffforge.feature_chords.presentation

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.feature_chords.domain.model.Chord
import com.riffforge.feature_chords.domain.util.ChordLibrary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordDictionaryScreen(
    onNavigateUp: () -> Unit,
    viewModel: ChordDictionaryViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diccionario de Acordes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(ChordLibrary.roots) { root ->
                    val isSelected = state.selectedRoot == root
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.onEvent(ChordDictionaryEvent.SelectRoot(root)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = root,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(ChordLibrary.types) { type ->
                    val isSelected = state.selectedType == type
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.onEvent(ChordDictionaryEvent.SelectType(type)) }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${state.selectedRoot} ${state.selectedType}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.currentChord != null) {
                        ChordDiagramCanvas(chord = state.currentChord)
                    } else {
                        Box(modifier = Modifier.height(300.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Diagrama no disponible aún",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChordDiagramCanvas(chord: Chord) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 200.dp, height = 280.dp)) {
            val numStrings = 6
            val numFretsToShow = 5

            val stringSpacing = size.width / (numStrings - 1)
            val fretSpacing = (size.height - 40.dp.toPx()) / numFretsToShow

            val startY = 40.dp.toPx()

            for (i in 0..numFretsToShow) {
                val y = startY + (i * fretSpacing)
                val isNut = i == 0 && chord.startingFret == 1
                drawLine(
                    color = onSurfaceColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isNut) 12f else 4f
                )
            }

            for (i in 0 until numStrings) {
                val x = i * stringSpacing
                drawLine(
                    color = onSurfaceColor,
                    start = Offset(x, startY),
                    end = Offset(x, startY + (numFretsToShow * fretSpacing)),
                    strokeWidth = 4f
                )
            }

            if (chord.startingFret > 1) {
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${chord.startingFret}fr",
                        -40f,
                        startY + (fretSpacing / 2) + 15f,
                        android.graphics.Paint().apply {
                            color = onSurfaceColor.toArgb()
                            textSize = 36f
                            isAntiAlias = true
                        }
                    )
                }
            }

            chord.frets.forEachIndexed { index, fretVal ->
                val x = index * stringSpacing
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = 40f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    if (fretVal == -1) {
                        paint.color = errorColor.toArgb()
                        drawText("X", x, startY - 20f, paint)
                    } else if (fretVal == 0) {
                        paint.color = onSurfaceColor.toArgb()
                        drawText("O", x, startY - 20f, paint)
                    }
                }
            }

            chord.frets.forEachIndexed { index, fretVal ->
                if (fretVal > 0) {
                    val visualFret = fretVal - chord.startingFret + 1

                    if (visualFret in 1..numFretsToShow) {
                        val cx = index * stringSpacing
                        val cy = startY + ((visualFret - 1) * fretSpacing) + (fretSpacing / 2)

                        drawCircle(
                            color = primaryColor,
                            radius = 24f,
                            center = Offset(cx, cy)
                        )

                        val fingerNum = chord.fingers[index]
                        if (fingerNum > 0) {
                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    fingerNum.toString(),
                                    cx,
                                    cy + 12f,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 32f
                                        isAntiAlias = true
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    }
                                )
                            }
                        }
                    }
                }
            }

            val fretGroups = chord.fingers.zip(chord.frets)
                .mapIndexed { i, pair -> Triple(i, pair.first, pair.second) }
                .filter { it.second > 0 && it.third > 0 }
                .groupBy { it.second }

            fretGroups.forEach { (_, group) ->
                if (group.size > 1) {
                    val fret = group.first().third
                    val visualFret = fret - chord.startingFret + 1
                    val firstString = group.minOf { it.first }
                    val lastString = group.maxOf { it.first }

                    val startX = firstString * stringSpacing
                    val endX = lastString * stringSpacing
                    val y = startY + ((visualFret - 1) * fretSpacing) + (fretSpacing / 2)

                    drawLine(
                        color = primaryColor,
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 48f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            group.first().second.toString(),
                            startX + ((endX - startX) / 2),
                            y + 12f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 32f
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                        )
                    }
                }
            }
        }
    }
}