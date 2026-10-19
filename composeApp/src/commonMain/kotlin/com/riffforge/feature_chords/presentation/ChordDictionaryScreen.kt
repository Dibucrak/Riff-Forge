package com.riffforge.feature_chords.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import com.riffforge.shared.domain.model.Chord
import com.riffforge.shared.domain.util.ChordLibrary
import com.riffforge.shared.domain.util.ChordTransposer

/**
 * Unified Compose Multiplatform Chord Dictionary Screen.
 * Runs on Android, iOS, and macOS with 100% native Compose Canvas drawing.
 */
@Composable
fun ChordDictionaryScreen(
    onNavigateUp: () -> Unit
) {
    var selectedRoot by remember { mutableStateOf("C") }
    var selectedType by remember { mutableStateOf("Mayor") }
    val useSpanish = AppSettings.useSpanishNotation

    val currentChord = remember(selectedRoot, selectedType) {
        ChordLibrary.chords.find { it.root == selectedRoot && it.type == selectedType }
    }

    val scrollState = rememberScrollState()

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(scrollState)
        ) {
            // ── Top Bar ───────────────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassButton(
                        onClick = onNavigateUp,
                        cornerRadius = 14.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = TextOnGlassPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Diccionario de Acordes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextOnGlassPrimary
                    )
                }

                // Spanish/American notation toggle
                GlassButton(
                    onClick = { AppSettings.toggleSpanishNotation() },
                    cornerRadius = 14.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Idioma de notas",
                            tint = AccentTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (useSpanish) "Do-Re" else "C-D",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentTint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Root Note Selector ────────────────────────────────────
            Text(
                text = "Tónica",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextOnGlassSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ChordLibrary.roots) { root ->
                    val isSelected = root == selectedRoot
                    val displayRoot = ChordTransposer.getNoteDisplayName(root, useSpanish)

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) AccentTint
                                else GlassSurfaceDim
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedRoot = root
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayRoot,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) OnAccent else TextOnGlassPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Chord Type Selector ───────────────────────────────────
            Text(
                text = "Tipo de Acorde",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextOnGlassSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ChordLibrary.types) { type ->
                    val isSelected = type == selectedType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) AccentTint
                                else GlassSurfaceDim
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedType = type
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OnAccent else TextOnGlassPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Chord Display Card ────────────────────────────────────
            GlassCardElevated(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayRoot = ChordTransposer.getNoteDisplayName(selectedRoot, useSpanish)
                    Text(
                        text = "$displayRoot $selectedType",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentTint
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentChord != null) {
                        ChordDiagramCanvas(chord = currentChord)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chord metadata chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            GlassStatChip(
                                modifier = Modifier.weight(1f),
                                label = "Dificultad",
                                value = currentChord.difficulty,
                                accentColor = SystemYellow
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            GlassStatChip(
                                modifier = Modifier.weight(1f),
                                label = "Cejilla",
                                value = if (currentChord.isBarre) "Traste ${currentChord.barreFret}" else "No",
                                accentColor = SystemGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            GlassStatChip(
                                modifier = Modifier.weight(1f),
                                label = "Posición",
                                value = "T ${currentChord.startingFret}",
                                accentColor = AccentTint
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Diagrama en construcción",
                                color = TextOnGlassSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Pure Compose Multiplatform Canvas drawing for guitar chords.
 * Uses Compose TextMeasurer for drawing labels without platform-specific dependencies.
 */
@Composable
fun ChordDiagramCanvas(chord: Chord) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = AccentTint
    val onSurfaceColor = TextOnGlassPrimary
    val onPrimaryColor = OnAccent
    val errorColor = SystemRed
    val openStringColor = SystemGreen
    val fretLabelColor = SystemYellow

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 200.dp, height = 250.dp)) {
            val numStrings = 6
            val numFretsToShow = 5

            val stringSpacing = size.width / (numStrings - 1)
            val startY = 36.dp.toPx()
            val fretSpacing = (size.height - startY - 16.dp.toPx()) / numFretsToShow

            // Draw horizontal frets
            for (i in 0..numFretsToShow) {
                val y = startY + (i * fretSpacing)
                val isNut = i == 0 && chord.startingFret == 1
                drawLine(
                    color = if (isNut) onSurfaceColor else onSurfaceColor.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isNut) 8f else 3f
                )
            }

            // Draw vertical guitar strings (6th to 1st)
            for (i in 0 until numStrings) {
                val x = i * stringSpacing
                // String thickness decreases from low E (thick) to high E (thin)
                val stringWidth = 5f - (i * 0.6f)
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.85f),
                    start = Offset(x, startY),
                    end = Offset(x, startY + (numFretsToShow * fretSpacing)),
                    strokeWidth = stringWidth.coerceAtLeast(1.5f)
                )
            }

            // Draw starting fret label if beyond fret 1
            if (chord.startingFret > 1) {
                val fretLabel = "${chord.startingFret}fr"
                val textLayout = textMeasurer.measure(
                    text = fretLabel,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = fretLabelColor
                    )
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(-textLayout.size.width.toFloat() - 8f, startY + (fretSpacing / 2f) - (textLayout.size.height / 2f))
                )
            }

            // Draw Open (O) and Muted (X) string markers
            chord.frets.forEachIndexed { index, fretVal ->
                val x = index * stringSpacing
                if (fretVal == -1) {
                    val textLayout = textMeasurer.measure(
                        text = "✕",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = errorColor
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(x - (textLayout.size.width / 2f), startY - 26.dp.toPx())
                    )
                } else if (fretVal == 0) {
                    val textLayout = textMeasurer.measure(
                        text = "○",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = openStringColor
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(x - (textLayout.size.width / 2f), startY - 26.dp.toPx())
                    )
                }
            }

            // Draw Barre chord if present
            if (chord.isBarre && chord.barreFret > 0) {
                val visualFret = chord.barreFret - chord.startingFret + 1
                if (visualFret in 1..numFretsToShow) {
                    val y = startY + ((visualFret - 1) * fretSpacing) + (fretSpacing / 2f)

                    // Find range of strings covered by the barre
                    val barreStrings = chord.frets.mapIndexedNotNull { i, f -> if (f >= chord.barreFret) i else null }
                    if (barreStrings.isNotEmpty()) {
                        val minString = barreStrings.minOrNull() ?: 0
                        val maxString = barreStrings.maxOrNull() ?: (numStrings - 1)
                        val startX = minString * stringSpacing
                        val endX = maxString * stringSpacing

                        drawLine(
                            color = primaryColor.copy(alpha = 0.85f),
                            start = Offset(startX, y),
                            end = Offset(endX, y),
                            strokeWidth = 36f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draw Fingered frets (dots with finger numbers)
            chord.frets.forEachIndexed { index, fretVal ->
                if (fretVal > 0) {
                    val visualFret = fretVal - chord.startingFret + 1
                    if (visualFret in 1..numFretsToShow) {
                        val cx = index * stringSpacing
                        val cy = startY + ((visualFret - 1) * fretSpacing) + (fretSpacing / 2f)

                        drawCircle(
                            color = primaryColor,
                            radius = 18f,
                            center = Offset(cx, cy)
                        )

                        // Draw finger index inside the dot if available
                        val fingerNum = chord.fingers.getOrNull(index) ?: 0
                        if (fingerNum > 0) {
                            val textLayout = textMeasurer.measure(
                                text = fingerNum.toString(),
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onPrimaryColor
                                )
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(cx - (textLayout.size.width / 2f), cy - (textLayout.size.height / 2f))
                            )
                        }
                    }
                }
            }
        }
    }
}
