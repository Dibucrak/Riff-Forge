package com.riffforge.feature_tools.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_chords.presentation.ChordDictionaryScreen
import com.riffforge.feature_daily_learning.presentation.DailyLearningScreen
import com.riffforge.feature_metronome.presentation.MetronomeScreen
import com.riffforge.feature_theory.presentation.circle_of_fifths.CircleOfFifthsScreen
import com.riffforge.feature_theory.presentation.progressions.ProgressionScreen
import com.riffforge.feature_theory.presentation.scales.ScalesScreen

enum class ToolDestination {
    None, CircleOfFifths, Metronome, Scales, Progressions, ChordDictionary, DailyLearning
}

data class ToolItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val destination: ToolDestination
)

/**
 * Liquid Glass Tools Screen — the tools and theory hub with frosted glass aesthetic.
 *
 * Visual design:
 *   - Full-screen vibrant gradient background with ambient glow orbs
 *   - Header with title and subtitle
 *   - Each tool is displayed as a frosted glass card with an accent-colored icon
 *   - Cards have gradient edge highlights and soft colored shadows
 *   - Sub-screens (metronome, scales, etc.) are embedded via when block
 */
@Composable
fun ToolsScreen() {
    var selectedTool by remember { mutableStateOf(ToolDestination.None) }

    val tools = listOf(
        ToolItem("Diccionario de Acordes", "Encuentra diagramas interactivos en el diapasón", Icons.Default.Book, AccentTint, ToolDestination.ChordDictionary),
        ToolItem("Aprendizaje Diario", "Descubre el acorde del día y retos teóricos", Icons.Default.Lightbulb, SystemYellow, ToolDestination.DailyLearning),
        ToolItem("Constructor de Escalas", "Visualiza patrones y modos en el mástil", Icons.Default.MusicNote, SystemGreen, ToolDestination.Scales),
        ToolItem("Metrónomo", "Mantén el tempo y ritmo con audio preciso", Icons.Default.Timer, SystemRed, ToolDestination.Metronome),
        ToolItem("Generador de Progresiones", "Encuentra inspiración armónica y secuencias", Icons.Default.ArrowRight, AccentTint, ToolDestination.Progressions),
        ToolItem("Círculo de Quintas", "Explora tonalidades relativas y armaduras", Icons.Default.School, AccentTint, ToolDestination.CircleOfFifths)
    )

    when (selectedTool) {
        ToolDestination.ChordDictionary -> {
            ChordDictionaryScreen(onNavigateUp = { selectedTool = ToolDestination.None })
        }
        ToolDestination.DailyLearning -> {
            DailyLearningScreen(onNavigateUp = { selectedTool = ToolDestination.None })
        }
        ToolDestination.CircleOfFifths -> {
            CircleOfFifthsScreen(onNavigateUp = { selectedTool = ToolDestination.None })
        }
        ToolDestination.Metronome -> {
            MetronomeScreen(onNavigateUp = { selectedTool = ToolDestination.None })
        }
        ToolDestination.Scales -> {
            ScalesScreen(onNavigateUp = { selectedTool = ToolDestination.None })
        }
        ToolDestination.Progressions -> {
            ProgressionScreen(onNavigateUp = { selectedTool = ToolDestination.None })
        }
        ToolDestination.None -> {
            GlassBackground {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {

                    // ── Header ────────────────────────────────────────
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Herramientas y Teoría",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextOnGlassPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Todo lo que necesitas para practicar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnGlassSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Tools List ────────────────────────────────────
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tools) { tool ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { selectedTool = tool.destination }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Icon with accent background
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = tool.accentColor.copy(alpha = 0.2f),
                                                shape = MaterialTheme.shapes.medium
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = null,
                                            tint = tool.accentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Text content
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tool.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextOnGlassPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tool.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextOnGlassSecondary
                                        )
                                    }

                                    // Chevron indicator
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Abrir",
                                        tint = TextOnGlassDim,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Info text ─────────────────────────────────────
                    Text(
                        text = "Las herramientas de teoría musical te ayudarán a mejorar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnGlassDim
                    )
                }
            }
        }
    }
}
