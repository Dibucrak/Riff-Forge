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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riffforge.feature_theory.domain.model.Scale
import com.riffforge.feature_theory.domain.util.ScaleLibrary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScalesScreen(
    onNavigateUp: () -> Unit
) {
    var selectedRoot by remember { mutableStateOf("C") }
    var selectedScale by remember { mutableStateOf(ScaleLibrary.scales[0]) }

    // Calculate target note indices for the selected root and scale
    val rootIndex = ScaleLibrary.roots.indexOf(selectedRoot)
    val targetNotesIndices = selectedScale.intervals.map { (rootIndex + it) % 12 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Constructor de Escalas", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(ScaleLibrary.roots) { root ->
                        val isSelected = selectedRoot == root
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedRoot = root },
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
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(ScaleLibrary.scales) { scale ->
                        val isSelected = selectedScale.name == scale.name
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedScale = scale }
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = scale.name,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scale details card
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = selectedScale.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Show the notes in the scale
                        val notes = selectedScale.intervals.map { i -> ScaleLibrary.roots[(rootIndex + i) % 12] }
                        Text(
                            text = "Notas: ${notes.joinToString(", ")}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Mapa del Diapasón",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp)
                ) {
                    FretboardCanvas(
                        targetNotesIndices = targetNotesIndices,
                        rootIndex = rootIndex
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FretboardCanvas(targetNotesIndices: List<Int>, rootIndex: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = Modifier.size(width = 900.dp, height = 200.dp)) {
        val numStrings = 6
        val numFrets = 12

        val stringSpacing = size.height / (numStrings + 1)
        val fretSpacing = size.width / (numFrets + 1)
        val tuningIndices = listOf(4, 11, 7, 2, 9, 4)

        val markers = listOf(3, 5, 7, 9, 12)
        markers.forEach { fret ->
            val cx = (fret * fretSpacing) - (fretSpacing / 2) + fretSpacing
            val cy = size.height / 2

            if (fret == 12) {
                drawCircle(color = surfaceVariantColor, radius = 12f, center = Offset(cx, cy - stringSpacing))
                drawCircle(color = surfaceVariantColor, radius = 12f, center = Offset(cx, cy + stringSpacing))
            } else {
                drawCircle(color = surfaceVariantColor, radius = 16f, center = Offset(cx, cy))
            }
        }

        // Draw strings
        for (i in 0 until numStrings) {
            val y = stringSpacing * (i + 1)
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.5f),
                start = Offset(fretSpacing, y),
                end = Offset(size.width, y),
                strokeWidth = 3f + (i * 0.8f)
            )
        }

        // Draw frets
        for (i in 1..numFrets + 1) {
            val x = i * fretSpacing
            val isNut = i == 1
            drawLine(
                color = onSurfaceColor,
                start = Offset(x, stringSpacing),
                end = Offset(x, stringSpacing * numStrings),
                strokeWidth = if (isNut) 12f else 6f
            )
        }

        // Draw note positions
        for (string in 0 until numStrings) {
            for (fret in 0..numFrets) {
                val noteIndex = (tuningIndices[string] + fret) % 12

                if (targetNotesIndices.contains(noteIndex)) {
                    val isRoot = noteIndex == rootIndex

                    val x = if (fret == 0) {
                        fretSpacing - 40f
                    } else {
                        (fret * fretSpacing) + (fretSpacing / 2)
                    }
                    val y = stringSpacing * (string + 1)

                    drawCircle(
                        color = if (isRoot) secondaryColor else primaryColor,
                        radius = 28f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
