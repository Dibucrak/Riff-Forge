package com.riffforge.feature_tuner.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*

/**
 * Liquid Glass Tuner Screen — the guitar tuner with frosted glass aesthetic.
 *
 * Visual design:
 *   - Full-screen vibrant gradient background with ambient glow orbs
 *   - Centered frosted glass card displaying the current note with large typography
 *   - Note text uses a vibrant accent color (cyan) when listening, dimmed otherwise
 *   - Start/Stop button uses the accent gradient glass button
 *   - Frequency display sits below the note in a secondary glass text style
 */
@Composable
fun TunerScreen() {
    var isListening by remember { mutableStateOf(false) }
    var currentNote by remember { mutableStateOf("-") }
    var frequency by remember { mutableStateOf(0f) }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Header Title ─────────────────────────────────────────
            Text(
                text = "Afinador",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextOnGlassPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Detecta la nota de tu guitarra",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnGlassSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Note Display Card ────────────────────────────────────
            GlassCardElevated(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Note letter
                    Text(
                        text = currentNote,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) AccentTint else TextOnGlassSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Frequency
                    Text(
                        text = "${frequency.toFloat().toInt()} Hz",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextOnGlassSecondary
                    )

                    // Status indicator
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isListening) SystemGreen else TextOnGlassDim,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isListening) "Escuchando..." else "Detenido",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isListening) SystemGreen else TextOnGlassDim
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Start / Stop Button ──────────────────────────────────
            if (isListening) {
                GlassButton(
                    onClick = {
                        isListening = false
                        currentNote = "-"
                        frequency = 0f
                    },
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    backgroundColor = SystemRed.copy(alpha = 0.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.MicNone,
                        contentDescription = "Detener",
                        tint = SystemRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detener",
                        color = SystemRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                GlassButtonAccent(
                    onClick = {
                        isListening = true
                        currentNote = "A"
                        frequency = 440f
                    },
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Iniciar",
                        tint = OnAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Iniciar Afinador",
                        color = OnAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Info text ────────────────────────────────────────────
            Text(
                text = "La función de afinación requiere acceso al micrófono.",
                style = MaterialTheme.typography.bodySmall,
                color = TextOnGlassDim
            )
        }
    }
}
