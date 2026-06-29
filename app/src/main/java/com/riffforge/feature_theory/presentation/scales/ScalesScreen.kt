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
import com.riffforge.feature_theory.domain.util.ScaleLibrary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScalesScreen(
    onNavigateUp: () -> Unit,
    viewModel: ScalesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(ScaleLibrary.roots) { root ->
                    val isSelected = state.selectedRoot == root
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.onEvent(ScalesEvent.SelectRoot(root)) },
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
                    val isSelected = state.selectedScale?.name == scale.name
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.onEvent(ScalesEvent.SelectScale(scale)) }
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

            // Detalles Teóricos
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = state.selectedScale?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    targetNotesIndices = state.targetNotesIndices,
                    rootIndex = ScaleLibrary.roots.indexOf(state.selectedRoot)
                )
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
        val stringNames = listOf("e", "B", "G", "D", "A", "E")

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

        for (i in 0 until numStrings) {
            val y = stringSpacing * (i + 1)
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.5f),
                start = Offset(fretSpacing, y),
                end = Offset(size.width, y),
                strokeWidth = 3f + (i * 0.8f)
            )

            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    stringNames[i],
                    fretSpacing / 2f,
                    y + 12f,
                    android.graphics.Paint().apply {
                        color = onSurfaceColor.toArgb()
                        textSize = 32f
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                )
            }
        }

        for (i in 1..numFrets + 1) {
            val x = i * fretSpacing
            val isNut = i == 1
            drawLine(
                color = onSurfaceColor,
                start = Offset(x, stringSpacing),
                end = Offset(x, stringSpacing * numStrings),
                strokeWidth = if (isNut) 12f else 6f
            )

            if (i > 1) {
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${i - 1}",
                        x - (fretSpacing / 2),
                        stringSpacing / 2f,
                        android.graphics.Paint().apply {
                            color = onSurfaceColor.toArgb()
                            textSize = 28f
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

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

                    val noteName = ScaleLibrary.roots[noteIndex]
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            noteName,
                            x,
                            y + 10f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 28f
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