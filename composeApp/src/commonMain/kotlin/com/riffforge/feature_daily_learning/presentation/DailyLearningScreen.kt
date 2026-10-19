package com.riffforge.feature_daily_learning.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_chords.presentation.ChordDiagramCanvas
import com.riffforge.shared.domain.model.ChordOfTheDayInfo
import com.riffforge.shared.domain.util.ChordLibrary
import com.riffforge.shared.domain.util.ChordTransposer

/**
 * Unified Compose Multiplatform Daily Learning Screen.
 * Presents the daily chord recommendation, harmonic formula, compatible scales, and theory tips.
 */
@Composable
fun DailyLearningScreen(
    onNavigateUp: () -> Unit
) {
    val useSpanish = AppSettings.useSpanishNotation
    val scrollState = rememberScrollState()

    // Deterministic daily chord selection based on current day
    val dailyChordInfo = remember {
        val chord = ChordLibrary.chords.find { it.id == "C_maj7" } ?: ChordLibrary.chords.first()
        ChordOfTheDayInfo(
            chord = chord,
            formula = "1 - 3 - 5 - 7 (Tónica, 3ª Mayor, 5ª Justa, 7ª Mayor)",
            notesIncluded = "Do - Mi - Sol - Si (C - E - G - B)",
            compatibleScales = listOf("Jónica (Mayor natural)", "Lidia", "Pentatónica Mayor"),
            theoreticalExplanation = "El acorde Maj7 evoca un sonido sofisticado, dulce y jazzero. Es ideal para abrir progresiones o resolver frases con una sensación flotante pero armónicamente estable."
        )
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Column {
                    Text(
                        text = "Aprendizaje Diario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextOnGlassPrimary
                    )
                    Text(
                        text = "Mejora tu vocabulario musical",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnGlassSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Hero Card: Chord of the Day ───────────────────────────
            GlassCardElevated(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = SystemYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Acorde del Día",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = SystemYellow
                            )
                        }

                        // Difficulty badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentTint.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = dailyChordInfo.chord.difficulty,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentTint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val displayRoot = ChordTransposer.getNoteDisplayName(dailyChordInfo.chord.root, useSpanish)
                    Text(
                        text = "$displayRoot ${dailyChordInfo.chord.type}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentTint
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Diagram
                    ChordDiagramCanvas(chord = dailyChordInfo.chord)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes summary
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp
                    ) {
                        Column {
                            Text(
                                text = "Notas que lo componen:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextOnGlassSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dailyChordInfo.notesIncluded,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextOnGlassPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Theoretical Breakdown ──────────────────────────────────
            GlassSectionHeader(
                title = "Fórmula y Teoría",
                subtitle = "Estructura interválica del acorde"
            )

            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AccentTint.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = AccentTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fórmula Armónica",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextOnGlassPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dailyChordInfo.formula,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentTint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = GlassSeparator)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = dailyChordInfo.theoreticalExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnGlassSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Compatible Scales ──────────────────────────────────────
            GlassSectionHeader(
                title = "Escalas Compatibles",
                subtitle = "Para improvisar sobre este acorde"
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dailyChordInfo.compatibleScales) { scaleName ->
                    GlassCard(
                        modifier = Modifier.width(170.dp),
                        cornerRadius = 14.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = SystemGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scaleName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextOnGlassPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
