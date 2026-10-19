package com.riffforge.feature_metronome.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Liquid Glass Metronome Screen — metronome with frosted glass aesthetic.
 *
 * Visual design:
 *   - Full-screen vibrant gradient background with ambient glow orbs
 *   - Frosted glass header bar with back button
 *   - Beat indicators use accent-colored circles on glass surface
 *   - BPM display is large and centered with cyan accent
 *   - Slider and controls sit within a glass card
 *   - Play/Pause button uses the accent gradient glass button
 */
@Composable
fun MetronomeScreen(
    onNavigateUp: () -> Unit
) {
    var bpm by remember { mutableIntStateOf(120) }
    var isPlaying by remember { mutableStateOf(false) }
    var beatsPerMeasure by remember { mutableIntStateOf(4) }
    var currentBeat by remember { mutableIntStateOf(0) }

    val currentIsPlaying by rememberUpdatedState(isPlaying)
    val currentBpm by rememberUpdatedState(bpm)
    val currentBeatsPerMeasure by rememberUpdatedState(beatsPerMeasure)

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val intervalMs = (60000L / currentBpm)
            currentBeat = 1
            playMetronomeClick(true, 1)
            delay(intervalMs)
            while (isActive && currentIsPlaying) {
                currentBeat = if (currentBeat >= currentBeatsPerMeasure) 1 else currentBeat + 1
                playMetronomeClick(currentBeat == 1, currentBeat)
                delay(intervalMs)
            }
            currentBeat = 0
        }
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {

            // ── Header Bar ───────────────────────────────────────────
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateUp,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = AccentTint.copy(alpha = 0.3f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Regresar",
                                tint = AccentTint
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Metrónomo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextOnGlassPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Beat Indicators ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..beatsPerMeasure) {
                    val isActive = i == currentBeat && isPlaying
                    val isFirstBeat = i == 1

                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(if (isActive) 32.dp else 24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive && isFirstBeat -> AccentTint
                                    isActive -> AccentTint
                                    else -> GlassSurface
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── BPM Display ──────────────────────────────────────────
            GlassCardElevated(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                ) {
                    Text(
                        text = bpm.toString(),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentTint
                    )
                    Text(
                        text = "BPM",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextOnGlassSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── BPM Controls ─────────────────────────────────────────
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GlassButton(
                        onClick = { if (bpm > 40) bpm-- },
                        cornerRadius = 12.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Reducir BPM",
                            tint = SystemRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Slider(
                        value = bpm.toFloat(),
                        onValueChange = { bpm = it.toInt() },
                        valueRange = 40f..240f,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentTint,
                            activeTrackColor = AccentTint,
                            inactiveTrackColor = TextOnGlassDim
                        )
                    )

                    GlassButton(
                        onClick = { if (bpm < 240) bpm++ },
                        cornerRadius = 12.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar BPM",
                            tint = SystemGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Time Signature Chips ─────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val timeSignatures = listOf(2, 3, 4, 6)
                timeSignatures.forEach { beats ->
                    val isSelected = beatsPerMeasure == beats
                    GlassStatChip(
                        label = "",
                        value = "$beats/4",
                        accentColor = if (isSelected) AccentTint else TextOnGlassDim,
                        cornerRadius = 20.dp
                    )
                    // Clickable wrapper
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Play / Pause Button ──────────────────────────────────
            if (isPlaying) {
                GlassButton(
                    onClick = { isPlaying = false },
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    backgroundColor = SystemRed.copy(alpha = 0.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pausar",
                        tint = SystemRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pausar",
                        color = SystemRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                GlassButtonAccent(
                    onClick = { isPlaying = true },
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = OnAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Iniciar",
                        color = OnAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Platform-agnostic metronome click using expect/actual
expect fun playMetronomeClick(isAccent: Boolean, beat: Int)
