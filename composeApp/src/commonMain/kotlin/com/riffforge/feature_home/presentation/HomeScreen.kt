package com.riffforge.feature_home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*

// ── Data Models ────────────────────────────────────────────────────────────

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
)

data class RecentRiffItem(
    val id: Int,
    val title: String,
    val key: String,
    val tempo: Int,
    val lastPlayed: String
)

// ── Screen ─────────────────────────────────────────────────────────────────

/**
 * Liquid Glass Home Screen — the main dashboard for Riff Forge.
 *
 * Visual design:
 *   - Full-screen vibrant gradient background with ambient glow orbs
 *   - Overlapping frosted glass panels at varying elevations creating volumetric depth
 *   - Each panel has a 0.5px gradient edge highlight simulating specular light
 *   - Soft colored shadows (purple/cyan) instead of harsh black elevation
 *   - Stat chips with accent-colored borders for quick-glance metrics
 *   - Horizontal scrolling glass cards for recent riffs
 *   - Primary CTA uses an accent gradient glass button
 */
@Composable
fun HomeScreen() {
    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {

            // ── Header / Greeting ──────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnGlassSecondary
                    )
                    Text(
                        text = "Riff Forge",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextOnGlassPrimary
                    )
                }

                // Avatar / Profile placeholder
                GlassButton(
                    onClick = { /* Profile */ },
                    cornerRadius = 20.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = AccentTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Stats Row ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GlassStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Riffs",
                    value = "12",
                    accentColor = AccentTint
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlassStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Práctica",
                    value = "3.5h",
                    accentColor = SystemGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlassStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Escala",
                    value = "Am",
                    accentColor = SystemRed
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Quick Actions ──────────────────────────────────────────
            GlassSectionHeader(
                title = "Acciones Rápidas",
                subtitle = "Crea y explora"
            )

            Spacer(modifier = Modifier.height(12.dp))

            val quickActions = listOf(
                QuickActionItem("Nuevo Riff", Icons.Default.AddCircle, AccentTint),
                QuickActionItem("Afinador", Icons.Default.Mic, AccentTint),
                QuickActionItem("Metrónomo", Icons.Default.Timer, SystemYellow),
                QuickActionItem("Escalas", Icons.Default.MusicNote, SystemGreen)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickActions) { action ->
                    GlassCardElevated(
                        modifier = Modifier
                            .width(140.dp)
                            .height(100.dp),
                        onClick = { /* Navigate */ }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = action.accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextOnGlassPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Featured / Hero Card ───────────────────────────────────
            GlassCardElevated(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* Continue last riff */ }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Continuar donde lo dejaste",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnGlassSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sunset Vibes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextOnGlassPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Dm · 120 BPM · Hace 2 horas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnGlassSecondary
                        )
                    }
                    GlassButtonAccent(
                        onClick = { /* Continue */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reproducir",
                            tint = OnAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jugar",
                            color = OnAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Recent Riffs ───────────────────────────────────────────
            GlassSectionHeader(
                title = "Riffs Recientes",
                subtitle = "Tu colección"
            )

            Spacer(modifier = Modifier.height(12.dp))

            val recentRiffs = listOf(
                RecentRiffItem(1, "Sunset Vibes", "Dm", 120, "Hace 2h"),
                RecentRiffItem(2, "Blues Shuffle", "A7", 95, "Hace 5h"),
                RecentRiffItem(3, "Metal Riff", "Em", 140, "Ayer"),
                RecentRiffItem(4, "Jazz Fusion", "Cmaj7", 110, "Hace 2 días")
            )

            recentRiffs.forEach { riff ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { /* Open riff */ }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = riff.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextOnGlassPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${riff.key}  ·  ${riff.tempo} BPM",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnGlassSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = riff.lastPlayed,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextOnGlassDim
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir",
                                tint = AccentTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
