package com.riffforge.feature_tools.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateToCircleOfFifths: () -> Unit,
    onNavigateToMetronome: () -> Unit,
    onNavigateToChordDictionary: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToEarTraining: () -> Unit,
    onNavigateToDailyLearning: () -> Unit,
    onNavigateToProgressions: () -> Unit,
    onNavigateToScales: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Herramientas y Teoría") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToolCardItem(
                title = "Constructor de Escalas",
                description = "Visualiza patrones y modos en el diapasón.",
                icon = Icons.Default.LinearScale,
                onClick = onNavigateToScales
            )

            ToolCardItem(
                title = "Diccionario de Acordes",
                description = "Encuentra diagramas interactivos.",
                icon = Icons.Default.LibraryBooks,
                onClick = onNavigateToChordDictionary
            )

            ToolCardItem(
                title = "Aprendizaje Diario",
                description = "Descubre el acorde del día y retos.",
                icon = Icons.Default.Lightbulb,
                onClick = onNavigateToDailyLearning
            )

            ToolCardItem(
                title = "Explorar Comunidad",
                description = "Descarga tablaturas de otros.",
                icon = Icons.Default.Public,
                onClick = onNavigateToCommunity
            )

            ToolCardItem(
                title = "Generador de Progresiones",
                description = "Encuentra inspiración armónica.",
                icon = Icons.Default.MusicVideo,
                onClick = onNavigateToProgressions
            )

            ToolCardItem(
                title = "Entrenador de Oído",
                description = "Afina tu percepción de intervalos.",
                icon = Icons.Default.Headphones,
                onClick = onNavigateToEarTraining
            )

            ToolCardItem(
                title = "Círculo de Quintas",
                description = "Explora tonalidades relativas.",
                icon = Icons.Default.Architecture,
                onClick = onNavigateToCircleOfFifths
            )

            ToolCardItem(
                title = "Metrónomo",
                description = "Mantén el tempo perfecto.",
                icon = Icons.Default.Timer,
                onClick = onNavigateToMetronome
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ToolCardItem(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}