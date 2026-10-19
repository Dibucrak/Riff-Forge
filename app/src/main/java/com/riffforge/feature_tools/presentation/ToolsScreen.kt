package com.riffforge.feature_tools.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*

/**
 * Liquid Glass Tools Screen — glassmorphic hub with frosted glass cards,
 * accent-colored icon backgrounds, and gradient edge highlights.
 */
@Composable
fun ToolsScreen(
    onNavigateToCircleOfFifths: () -> Unit,
    onNavigateToMetronome: () -> Unit,
    onNavigateToChordDictionary: () -> Unit,
    onNavigateToCloudBackup: () -> Unit,
    onNavigateToEarTraining: () -> Unit,
    onNavigateToDailyLearning: () -> Unit,
    onNavigateToProgressions: () -> Unit,
    onNavigateToScales: () -> Unit,
    isDarkTheme: Boolean
) {
    val scrollState = rememberScrollState()
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassSectionHeader(isDarkTheme = isDarkTheme, title = "Herramientas y Teoría")

        ToolCardItem(
            title = "Constructor de Escalas",
            description = "Visualiza patrones y modos en el diapasón.",
            icon = Icons.Default.LinearScale,
            accentColor = AccentTint,
            isDarkTheme = isDarkTheme,
            textColor = textColor,
            dimTextColor = dimTextColor,
            onClick = onNavigateToScales
        )
        ToolCardItem(
            title = "Diccionario de Acordes",
            description = "Encuentra diagramas interactivos.",
            icon = Icons.Default.LibraryBooks,
            accentColor = AccentTint,
            isDarkTheme = isDarkTheme,
            textColor = textColor,
            dimTextColor = dimTextColor,
            onClick = onNavigateToChordDictionary
        )
        ToolCardItem(
            title = "Aprendizaje Diario",
            description = "Descubre el acorde del día y retos.",
            icon = Icons.Default.Lightbulb,
            accentColor = SystemYellow,
            isDarkTheme = isDarkTheme,
            textColor = textColor,
            dimTextColor = dimTextColor,
            onClick = onNavigateToDailyLearning
        )
        ToolCardItem(
            title = "Respaldo y Restauración",
            description = "Respalda o restaura desde la nube.",
            icon = Icons.Default.Cloud,
            accentColor = SystemGreen,
            isDarkTheme = isDarkTheme,
            textColor = textColor,
            dimTextColor = dimTextColor,
            onClick = onNavigateToCloudBackup
        )
        ToolCardItem(
            title = "Generador de Progresiones",
            description = "Encuentra inspiración armónica.",
            icon = Icons.Default.MusicVideo,
            accentColor = SystemRed,
            isDarkTheme = isDarkTheme,
            textColor = textColor,
            dimTextColor = dimTextColor,
            onClick = onNavigateToProgressions
        )
        ToolCardItem(
            title = "Entrenador de Oído",
            description = "Afina tu percepción de intervalos.",
            icon = Icons.Default.Headphones,
            accentColor = AccentTint,
            isDarkTheme = isDarkTheme,
            textColor = textColor,
            dimTextColor = dimTextColor,
            onClick = onNavigateToEarTraining
        )
        ToolCardItem(
            title = "Círculo de Quintas",
            description = "Explora tonalidades relativas.",
            icon = Icons.Default.Architecture,
            accentColor = AccentTint,
            isDarkTheme = isDarkTheme,
            textColor = textColor,
            dimTextColor = dimTextColor,
            onClick = onNavigateToCircleOfFifths
        )
        ToolCardItem(
            title = "Metrónomo",
            description = "Mantén el tempo perfecto.",
            icon = Icons.Default.Timer,
            accentColor = SystemYellow,
            isDarkTheme = isDarkTheme,
            textColor = textColor,
            dimTextColor = dimTextColor,
            onClick = onNavigateToMetronome
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ToolCardItem(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    isDarkTheme: Boolean,
    textColor: Color,
    dimTextColor: Color,
    onClick: () -> Unit
) {
    val edgeColors = if (isDarkTheme) {
        listOf(GlassEdgeDarkStart, GlassEdgeDarkMid, GlassEdgeDarkEnd)
    } else {
        listOf(GlassEdgeHighlightStart, GlassEdgeHighlightMid, GlassEdgeHighlightEnd)
    }
    val backgroundColor = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = GlassShadow, ambientColor = GlassShadow)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            0.5.dp,
            Brush.linearGradient(
                colors = edgeColors,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(500f, 500f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = accentColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = dimTextColor
                )
            }
        }
    }
}