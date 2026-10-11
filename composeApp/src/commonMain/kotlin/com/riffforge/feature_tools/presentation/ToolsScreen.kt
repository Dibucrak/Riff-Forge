package com.riffforge.feature_tools.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.riffforge.feature_metronome.presentation.MetronomeScreen
import com.riffforge.feature_theory.presentation.circle_of_fifths.CircleOfFifthsScreen
import com.riffforge.feature_theory.presentation.progressions.ProgressionScreen
import com.riffforge.feature_theory.presentation.scales.ScalesScreen

enum class ToolDestination {
    None, CircleOfFifths, Metronome, Scales, Progressions
}

data class ToolItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val destination: ToolDestination
)

@Composable
fun ToolsScreen() {
    var selectedTool by remember { mutableStateOf(ToolDestination.None) }

    val tools = listOf(
        ToolItem("Círculo de Quintas", "Explora las relaciones entre tonos", Icons.Default.School, ToolDestination.CircleOfFifths),
        ToolItem("Metrónomo", "Mantén el ritmo", Icons.Default.Timer, ToolDestination.Metronome),
        ToolItem("Constructor de Escalas", "Visualiza patrones en el diapasón", Icons.Default.MusicNote, ToolDestination.Scales),
        ToolItem("Generador de Progresiones", "Encuentra inspiración armónica", Icons.Default.ArrowRight, ToolDestination.Progressions)
    )

    when (selectedTool) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Herramientas y Teoría",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(tools) { tool ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedTool = tool.destination }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = tool.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tool.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
