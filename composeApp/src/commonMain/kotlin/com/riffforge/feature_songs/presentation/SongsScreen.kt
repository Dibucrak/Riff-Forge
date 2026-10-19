package com.riffforge.feature_songs.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*

data class SongItem(
    val id: Int,
    val title: String,
    val artist: String
)

/**
 * Liquid Glass Songs Screen — the song library with frosted glass aesthetic.
 *
 * Visual design:
 *   - Full-screen vibrant gradient background with ambient glow orbs
 *   - Header with title and floating glass add button
 *   - Each song is displayed as a frosted glass card with gradient edge highlights
 *   - Empty state uses a centered glass panel with inviting copy
 *   - Soft colored shadows for depth instead of harsh Material elevation
 */
@Composable
fun SongsScreen() {
    var songs by remember { mutableStateOf(
        listOf(
            SongItem(1, "Stairway to Heaven", "Led Zeppelin"),
            SongItem(2, "Hotel California", "Eagles"),
            SongItem(3, "Sweet Child O' Mine", "Guns N' Roses"),
            SongItem(4, "Back in Black", "AC/DC"),
            SongItem(5, "Bohemian Rhapsody", "Queen")
        )
    )}

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {

            // ── Header ───────────────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mis Canciones",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextOnGlassPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${songs.size} canciones en tu colección",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnGlassSecondary
                    )
                }

                // Floating glass add button
                GlassButtonAccent(
                    onClick = { /* Add song */ },
                    cornerRadius = 16.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar canción",
                        tint = OnAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Song List ────────────────────────────────────────────
            if (songs.isEmpty()) {
                // ── Empty State ──────────────────────────────────────
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = AccentTint,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay canciones aún",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextOnGlassPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Agrega tu primera canción para comenzar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnGlassSecondary
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(songs) { song ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { /* Navigate to song detail */ }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextOnGlassPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextOnGlassSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir",
                                    tint = AccentTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Info text ────────────────────────────────────────────
            Text(
                text = "Las canciones se sincronizarán desde la base de datos.",
                style = MaterialTheme.typography.bodySmall,
                color = TextOnGlassDim
            )
        }
    }
}
