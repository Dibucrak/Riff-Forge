package com.riffforge.feature_daily_learning.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.presentation.components.GlassBackground
import com.riffforge.core.presentation.components.GlassCard
import com.riffforge.core.presentation.components.GlassCardElevated
import com.riffforge.core.presentation.components.GlassSectionHeader
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_chords.presentation.ChordDiagramCanvas
import com.riffforge.feature_songs.domain.util.ChordTransposer

@Composable
fun DailyLearningScreen(
    onNavigateUp: () -> Unit,
    onNavigateToEarTraining: () -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: DailyLearningViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val scrollState = rememberScrollState()
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight
    val glassSurface = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight
    val useSpanish = AppSettings.useSpanishNotation

    GlassBackground(isDarkTheme = isDarkTheme) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentTint)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top bar
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    isDarkTheme = isDarkTheme,
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
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
                            text = "Aprendizaje Diario",
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
                        .padding(start = 16.dp, top = 100.dp, end = 16.dp, bottom = 16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = AccentTint)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "El Acorde del Día",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    state.chordOfTheDay?.let { info ->
                        GlassCardElevated(
                            modifier = Modifier.fillMaxWidth(),
                            isDarkTheme = isDarkTheme,
                            cornerRadius = 24.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val displayRoot = ChordTransposer.getNoteDisplayName(info.chord.root, useSpanish)
                                Text(
                                    text = "$displayRoot ${info.chord.type}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentTint
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                ChordDiagramCanvas(chord = info.chord, isDarkTheme = isDarkTheme)

                                Spacer(modifier = Modifier.height(24.dp))

                                Divider(color = glassSurface.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(16.dp))

                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                    Text("Fórmula:", fontWeight = FontWeight.Bold, color = AccentTint)
                                    Text(info.formula, style = MaterialTheme.typography.bodyLarge, color = textColor)

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Escalas Compatibles:", fontWeight = FontWeight.Bold, color = AccentTint)
                                    info.compatibleScales.forEach { scale ->
                                        Text("• $scale", style = MaterialTheme.typography.bodyMedium, color = dimTextColor)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Teoría:", fontWeight = FontWeight.Bold, color = AccentTint)
                                    Text(info.theoreticalExplanation, style = MaterialTheme.typography.bodyMedium, color = dimTextColor)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    GlassSectionHeader(
                        isDarkTheme = isDarkTheme,
                        title = "Reto del Día"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCardElevated(
                        modifier = Modifier.fillMaxWidth(),
                        isDarkTheme = isDarkTheme,
                        onClick = { onNavigateToEarTraining() },
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentTint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = AccentTint
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Entrenamiento Auditivo", fontWeight = FontWeight.Bold, color = textColor)
                                Text("Completa 5 ejercicios correctamente hoy.", style = MaterialTheme.typography.bodyMedium, color = dimTextColor)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}