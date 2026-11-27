package com.riffforge.feature_tools.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator

data class ToolEntry(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Tools Screen matching Android 1:1:
 * Inset ElevatedCards (24dp), theme-matched primary icons, section titles inside card,
 * and clean divided rows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    navigator: Navigator? = null,
    onNavigateToCircleOfFifths: () -> Unit = { navigator?.push(CircleOfFifthsToolScreen()) },
    onNavigateToMetronome: () -> Unit = { navigator?.push(MetronomeToolScreen()) },
    onNavigateToChordDictionary: () -> Unit = { navigator?.push(ChordDictionaryToolScreen()) },
    onNavigateToCloudBackup: () -> Unit = { navigator?.push(CloudBackupToolScreen()) },
    onNavigateToEarTraining: () -> Unit = { navigator?.push(EarTrainingToolScreen()) },
    onNavigateToDailyLearning: () -> Unit = { navigator?.push(DailyLearningToolScreen()) },
    onNavigateToProgressions: () -> Unit = { navigator?.push(ProgressionsToolScreen()) },
    onNavigateToScales: () -> Unit = { navigator?.push(ScalesToolScreen()) },
    onNavigateToCommunity: () -> Unit = { navigator?.push(CommunityToolScreen()) },
    onNavigateToAiChords: () -> Unit = { navigator?.push(AiChordsToolScreen()) }
) {
    val scrollState = rememberScrollState()

    val practiceTools = listOf(
        ToolEntry(
            title = "AI Chords (YouTube)",
            description = "Pega un link de YouTube y extrae acordes con IA.",
            icon = Icons.Default.AutoAwesome,
            onClick = onNavigateToAiChords
        ),
        ToolEntry(
            title = "Metrónomo",
            description = "Compás, tempo y tap tempo en tiempo real.",
            icon = Icons.Default.Timer,
            onClick = onNavigateToMetronome
        ),
        ToolEntry(
            title = "Diccionario de Acordes",
            description = "Diagramas interactivos, cejillas y digitación.",
            icon = Icons.Default.MenuBook,
            onClick = onNavigateToChordDictionary
        )
    )

    val theoryTools = listOf(
        ToolEntry(
            title = "Constructor de Escalas",
            description = "Visualiza patrones y modos en el diapasón.",
            icon = Icons.Default.MusicNote,
            onClick = onNavigateToScales
        ),
        ToolEntry(
            title = "Círculo de Quintas",
            description = "Explora armaduras, tónicas y tonos relativos.",
            icon = Icons.Default.School,
            onClick = onNavigateToCircleOfFifths
        ),
        ToolEntry(
            title = "Generador de Progresiones",
            description = "Secuencias armónicas inspiradoras por género.",
            icon = Icons.Default.AutoAwesome,
            onClick = onNavigateToProgressions
        )
    )

    val trainingTools = listOf(
        ToolEntry(
            title = "Aprendizaje Diario",
            description = "Descubre el acorde del día y retos armónicos.",
            icon = Icons.Default.Lightbulb,
            onClick = onNavigateToDailyLearning
        ),
        ToolEntry(
            title = "Entrenamiento de Oído",
            description = "Reconocimiento auditivo de intervalos y notas.",
            icon = Icons.Default.Headphones,
            onClick = onNavigateToEarTraining
        ),
        ToolEntry(
            title = "Comunidad Riff Forge",
            description = "Explora y descarga canciones compartidas por otros músicos.",
            icon = Icons.Default.Public,
            onClick = onNavigateToCommunity
        ),
        ToolEntry(
            title = "Sincronización en la Nube",
            description = "Respalda y restaura tus canciones y setlists.",
            icon = Icons.Default.CloudUpload,
            onClick = onNavigateToCloudBackup
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Herramientas & Teoría", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Grupo 1: Práctica & IA ───────────────────────────────
            ToolsGroupCard(
                groupTitle = "Práctica & IA",
                entries = practiceTools
            )

            // ── Grupo 2: Teoría & Armonía ──────────────────────────────
            ToolsGroupCard(
                groupTitle = "Teoría & Composición",
                entries = theoryTools
            )

            // ── Grupo 3: Entrenamiento & Rutinas ───────────────────────
            ToolsGroupCard(
                groupTitle = "Entrenamiento & Respaldo",
                entries = trainingTools
            )

            Spacer(modifier = Modifier.navigationBarsPadding().height(110.dp))
        }
    }
}

@Composable
private fun ToolsGroupCard(
    groupTitle: String,
    entries: List<ToolEntry>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = groupTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            entries.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { entry.onClick() }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = entry.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (index < entries.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 56.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
