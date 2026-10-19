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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_theory.domain.util.ScaleLibrary

@Composable
fun ScalesScreen(
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: ScalesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight
    val glassSurface = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight

    GlassBackground(isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top bar
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                isDarkTheme = isDarkTheme,
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateUp,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = glassSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = textColor)
                    }
                    Text(
                        text = "Constructor de Escalas",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Content area - below header (solid, nothing overlaps)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(ScaleLibrary.roots) { root ->
                        val isSelected = state.selectedRoot == root
                        val bg = if (isSelected) AccentTint else (if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim)
                        val textC = if (isSelected) OnAccent else textColor
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .clickable { viewModel.onEvent(ScalesEvent.SelectRoot(root)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = root, fontWeight = FontWeight.Bold, color = textC)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(ScaleLibrary.scales) { scale ->
                        val isSelected = state.selectedScale?.name == scale.name
                        val bg = if (isSelected) AccentTint else (if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim)
                        val textC = if (isSelected) OnAccent else dimTextColor
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bg)
                                .clickable { viewModel.onEvent(ScalesEvent.SelectScale(scale)) }
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = scale.name, fontWeight = FontWeight.Medium, color = textC)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glass card for description
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDarkTheme = isDarkTheme,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = state.selectedScale?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = dimTextColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mapa del Diapasón",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentTint
                )

                Spacer(modifier = Modifier.height(12.dp))

                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .horizontalScroll(scrollState)
                ) {
                    FretboardCanvas(
                        targetNotesIndices = state.targetNotesIndices,
                        rootIndex = ScaleLibrary.roots.indexOf(state.selectedRoot),
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FretboardCanvas(targetNotesIndices: List<Int>, rootIndex: Int, isDarkTheme: Boolean) {
    val primaryColor = AccentTint
    val secondaryColor = AccentTint
    val onSurfaceColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val surfaceVariantColor = if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim

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