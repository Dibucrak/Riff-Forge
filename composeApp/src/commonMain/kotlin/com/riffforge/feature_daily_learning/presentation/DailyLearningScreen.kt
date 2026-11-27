package com.riffforge.feature_daily_learning.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.riffforge.DailyDateInfo
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.currentDailyDateInfo
import com.riffforge.feature_chords.presentation.ChordDiagramCanvas
import com.riffforge.shared.domain.util.ChordLibrary
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.delay

private fun dailyChordInfo(type: String): Pair<String, String> = when (type) {
    "Mayor" -> "1 · 3 · 5" to "Mayor (Jónica), Pentatónica Mayor, Lidia"
    "Mayor (Cejilla)" -> "1 · 3 · 5" to "Mayor (Jónica), Pentatónica Mayor, Mixolidia"
    "Menor" -> "1 · b3 · 5" to "Menor (Eólica), Pentatónica Menor, Dórica"
    "7" -> "1 · 3 · 5 · b7" to "Mixolidia, Pentatónica Menor de Blues"
    "Maj7" -> "1 · 3 · 5 · 7" to "Mayor (Jónica), Lidia, Pentatónica Mayor"
    "Min7" -> "1 · b3 · 5 · b7" to "Dórica, Pentatónica Menor, Menor (Eólica)"
    "Sus4" -> "1 · 4 · 5" to "Mixolidia, Mayor (Jónica), Pentatónica Mayor"
    "Sus2" -> "1 · 2 · 5" to "Mayor (Jónica), Dórica, Pentatónica Mayor"
    "Dim" -> "1 · b3 · b5" to "Disminuida (Semitono-Tono), Locria, Menor Armónica"
    "Aug" -> "1 · 3 · #5" to "Tonos Enteros, Lidia Aumentada, Menor Melódica"
    else -> "1 · 3 · 5" to "Pentatónica, Escala Cromática"
}

private fun calculateStreakDays(
    completed: Set<String>,
    dateInfo: DailyDateInfo
): Int {
    if (completed.isEmpty()) return 0
    val startIso = when {
        dateInfo.isoDate in completed -> dateInfo.isoDate
        dateInfo.yesterdayIsoDate in completed -> dateInfo.yesterdayIsoDate
        else -> return 0
    }
    var count = 0
    var cursor = startIso
    while (cursor in completed && count < 365) {
        count++
        cursor = decrementIsoDate(cursor)
    }
    return count
}

private fun decrementIsoDate(iso: String): String {
    val parts = iso.split("-")
    if (parts.size != 3) return ""
    var year = parts[0].toIntOrNull() ?: return ""
    var month = parts[1].toIntOrNull() ?: return ""
    var day = parts[2].toIntOrNull() ?: return ""
    day -= 1
    if (day < 1) {
        month -= 1
        if (month < 1) {
            month = 12
            year -= 1
        }
        day = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
            else -> 30
        }
    }
    val yStr = year.toString().padStart(4, '0')
    val mStr = month.toString().padStart(2, '0')
    val dStr = day.toString().padStart(2, '0')
    return "$yStr-$mStr-$dStr"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLearningScreen(
    onNavigateUp: () -> Unit = {},
    onNavigateToEarTraining: () -> Unit = {}
) {
    val useSpanish = AppSettings.useSpanishNotation
    val dateInfo = remember { currentDailyDateInfo() }
    val settings = remember { Settings() }
    val dailyChord = remember(dateInfo.daySeed) {
        val idx = ((dateInfo.daySeed % ChordLibrary.chords.size) + ChordLibrary.chords.size) % ChordLibrary.chords.size
        ChordLibrary.chords[idx]
    }
    val (formulaText, scalesText) = remember(dailyChord.type) { dailyChordInfo(dailyChord.type) }

    var warmupDone by remember {
        mutableStateOf(settings.get("daily_warmup_date", "") == dateInfo.isoDate)
    }
    var chordChangeDone by remember {
        mutableStateOf(settings.get("daily_chord_date", "") == dateInfo.isoDate)
    }
    var completedDates by remember {
        val raw = settings.get("daily_completed_dates", "")
        mutableStateOf(raw.split(",").filter { it.isNotBlank() }.toSet())
    }
    var showChordChangeDialog by remember { mutableStateOf(false) }

    fun syncProgress(newWarmup: Boolean, newChordChange: Boolean) {
        warmupDone = newWarmup
        chordChangeDone = newChordChange
        val updated = if (newWarmup && newChordChange) {
            (completedDates + dateInfo.isoDate).sorted().takeLast(90).toSet()
        } else {
            completedDates - dateInfo.isoDate
        }
        completedDates = updated
        settings.set("daily_warmup_date", if (newWarmup) dateInfo.isoDate else "")
        settings.set("daily_chord_date", if (newChordChange) dateInfo.isoDate else "")
        settings.set("daily_completed_dates", updated.joinToString(","))
    }

    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
    val streakDays = remember(completedDates) { calculateStreakDays(completedDates, dateInfo) }

    if (showChordChangeDialog) {
        var timerRunning by remember { mutableStateOf(false) }
        var secondsLeft by remember { mutableStateOf(60) }
        var changeCount by remember { mutableStateOf(0) }

        LaunchedEffect(timerRunning) {
            while (timerRunning && secondsLeft > 0) {
                delay(1000L)
                secondsLeft--
                if (secondsLeft == 0) {
                    timerRunning = false
                    syncProgress(warmupDone, true)
                }
            }
        }

        val fromChord = NotationConverter.formatChord("F", useSpanish)
        val toChord = NotationConverter.formatChord("C", useSpanish)

        AlertDialog(
            onDismissRequest = { showChordChangeDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Cambio Rápido: $fromChord → $toChord",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Alterna limpiamente entre $fromChord (cejilla) y $toChord durante 1 minuto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${secondsLeft}s",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$changeCount cambios limpios",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (!timerRunning && secondsLeft == 0) {
                                    secondsLeft = 60
                                }
                                timerRunning = !timerRunning
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                when {
                                    timerRunning -> "Pausar"
                                    secondsLeft == 0 -> "Reiniciar 60s"
                                    else -> "Iniciar 60s"
                                }
                            )
                        }
                        Button(
                            onClick = { changeCount++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+1 Cambio")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        syncProgress(warmupDone, true)
                        showChordChangeDialog = false
                    }
                ) {
                    Text("Completar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChordChangeDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aprendizaje Diario", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Racha Card ───────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "$streakDays días seguidos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Meta diaria: 15 min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEachIndexed { idx, day ->
                            val dayIso = dateInfo.weekIsoDates[idx]
                            val isDone = dayIso in completedDates
                            val isToday = idx == dateInfo.dayOfWeekIndex && !isDone
                            val bg = when {
                                isDone -> SystemGreen
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                            }
                            val fg = when {
                                isDone -> Color.Black.copy(alpha = 0.85f)
                                isToday -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = fg
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Rutina de Hoy ────────────────────────────────────────────
            Text(
                text = "Rutina de Hoy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { syncProgress(!warmupDone, chordChangeDone) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (warmupDone) SystemGreen else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (warmupDone) Icons.Default.Check else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (warmupDone) Color.Black.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Calentamiento Cromático",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (warmupDone) "Completado · 5 min · 90 BPM" else "5 min · 90 BPM · Toca para marcar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )

                    val chordFrom = NotationConverter.formatChord("F", useSpanish)
                    val chordTo = NotationConverter.formatChord("C", useSpanish)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showChordChangeDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (chordChangeDone) SystemGreen else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (chordChangeDone) Icons.Default.Check else Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = if (chordChangeDone) Color.Black.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cambio Rápido $chordFrom → $chordTo",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "5 min · Practica transición de cejilla limpia",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEarTraining() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Entrenamiento Auditivo",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "5 min · Identifica intervalos de oído",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── El Acorde del Día Card ───────────────────────────────────
            val displayRoot = NotationConverter.formatChord(dailyChord.root, useSpanish)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Acorde del Día",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$displayRoot ${dailyChord.type}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        ChordDiagramCanvas(chord = dailyChord)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Fórmula: $formulaText",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Escalas compatibles: $scalesText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
