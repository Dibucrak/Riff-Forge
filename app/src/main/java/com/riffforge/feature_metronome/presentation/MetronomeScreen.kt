package com.riffforge.feature_metronome.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*

/**
 * Liquid Glass Metronome Screen — glassmorphic header, BPM display in elevated glass card,
 * accent-colored slider, and glass play/pause button.
 */
@Composable
fun MetronomeScreen(
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: MetronomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight

    DisposableEffect(key1 = true) {
        onDispose {
            if (viewModel.state.value.isPlaying) {
                viewModel.onEvent(MetronomeEvent.TogglePlayPause)
            }
        }
    }

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
                        text = "Metrónomo",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlassCardElevated(
                    modifier = Modifier.fillMaxWidth(),
                    isDarkTheme = isDarkTheme,
                    cornerRadius = 28.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Beat indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..state.beatsPerMeasure) {
                                val isActive = i == state.currentBeat && state.isPlaying
                                val isFirstBeat = i == 1
                                val beatColor = when {
                                    isActive && isFirstBeat -> AccentTint
                                    isActive -> AccentTint
                                    else -> (if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim)
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(if (isActive) 32.dp else 24.dp)
                                        .clip(CircleShape)
                                        .background(beatColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = state.bpm.toString(),
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "BPM",
                            style = MaterialTheme.typography.titleLarge,
                            color = dimTextColor
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // BPM controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val chipBg = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight
                            IconButton(
                                onClick = { if (state.bpm > 40) viewModel.onEvent(MetronomeEvent.ChangeBpm(state.bpm - 1)) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = chipBg)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Reducir BPM", tint = textColor)
                            }

                            Slider(
                                value = state.bpm.toFloat(),
                                onValueChange = { viewModel.onEvent(MetronomeEvent.ChangeBpm(it.toInt())) },
                                valueRange = 40f..240f,
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentTint,
                                    activeTrackColor = AccentTint,
                                    inactiveTrackColor = if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim
                                )
                            )

                            IconButton(
                                onClick = { if (state.bpm < 240) viewModel.onEvent(MetronomeEvent.ChangeBpm(state.bpm + 1)) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = chipBg)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Aumentar BPM", tint = textColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Time signature chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val timeSignatures = listOf(2, 3, 4, 6)
                            timeSignatures.forEach { beats ->
                                val isSelected = state.beatsPerMeasure == beats
                                val chipBg = if (isSelected) {
                                    if (isDarkTheme) GlassSurfaceElevatedDark else GlassSurfaceElevatedLight
                                } else Color.Transparent
                                val chipText = if (isSelected) textColor else dimTextColor
                                Box(
                                    modifier = Modifier
                                        .background(chipBg, RoundedCornerShape(50.dp))
                                        .clickable { viewModel.onEvent(MetronomeEvent.ChangeBeatsPerMeasure(beats)) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "$beats/4", color = chipText, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Play/Pause button
                        GlassButtonAccent(
                            onClick = { viewModel.onEvent(MetronomeEvent.TogglePlayPause) }
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Reproducir/Pausar",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}