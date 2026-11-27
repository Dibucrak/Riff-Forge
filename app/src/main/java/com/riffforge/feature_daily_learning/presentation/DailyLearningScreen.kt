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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.feature_chords.presentation.ChordDiagramCanvas
import com.riffforge.feature_songs.domain.util.ChordTransposer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

private data class DailyWeekSnapshot(
    val dayOfWeekIndex: Int,
    val isoDate: String,
    val yesterdayIsoDate: String,
    val weekIsoDates: List<String>
)

private fun currentWeekSnapshot(): DailyWeekSnapshot {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    val isoDate = fmt.format(cal.time)
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val dayIdx = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    val yCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val mCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -dayIdx) }
    val week = List(7) { offset ->
        val d = (mCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
        fmt.format(d.time)
    }
    return DailyWeekSnapshot(dayIdx, isoDate, fmt.format(yCal.time), week)
}

private fun calculateStreak(completed: Set<String>, todayIso: String, yesterdayIso: String): Int {
    if (completed.isEmpty()) return 0
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val startIso = when {
        todayIso in completed -> todayIso
        yesterdayIso in completed -> yesterdayIso
        else -> return 0
    }
    val cal = Calendar.getInstance()
    val parsed = runCatching { fmt.parse(startIso) }.getOrNull() ?: return 0
    cal.time = parsed
    var count = 0
    while (fmt.format(cal.time) in completed && count < 365) {
        count++
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return count
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLearningScreen(
    onNavigateUp: () -> Unit,
    onNavigateToEarTraining: () -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: DailyLearningViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val scrollState = rememberScrollState()
    val useSpanish = AppSettings.useSpanishNotation
    val context = LocalContext.current
    val prefs = remember(context) { AppSettings.prefs(context) }
    val dateSnapshot = remember { currentWeekSnapshot() }

    var warmupDone by remember {
        mutableStateOf(prefs.getString("daily_warmup_date", "") == dateSnapshot.isoDate)
    }
    var chordChangeDone by remember {
        mutableStateOf(prefs.getString("daily_chord_date", "") == dateSnapshot.isoDate)
    }
    var completedDates by remember {
        val raw = prefs.getString("daily_completed_dates", "") ?: ""
        mutableStateOf(raw.split(",").filter { it.isNotBlank() }.toSet())
    }
    var showChordChangeDialog by remember { mutableStateOf(false) }

    fun syncProgress(newWarmup: Boolean, newChordChange: Boolean) {
        warmupDone = newWarmup
        chordChangeDone = newChordChange
        val updatedDates = if (newWarmup && newChordChange) {
            (completedDates + dateSnapshot.isoDate).sorted().takeLast(90).toSet()
        } else {
            completedDates - dateSnapshot.isoDate
        }
        completedDates = updatedDates
        prefs.edit()
            .putString("daily_warmup_date", if (newWarmup) dateSnapshot.isoDate else "")
            .putString("daily_chord_date", if (newChordChange) dateSnapshot.isoDate else "")
            .putString("daily_completed_dates", updatedDates.joinToString(","))
            .apply()
    }

    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
    val streakDays = remember(completedDates) {
        calculateStreak(completedDates, dateSnapshot.isoDate, dateSnapshot.yesterdayIsoDate)
    }

    if (showChordChangeDialog) {
        var timerRunning by remember { mutableStateOf(false) }
        var secondsLeft by remember { mutableIntStateOf(60) }
        var changeCount by remember { mutableIntStateOf(0) }

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

        val fromChord = ChordTransposer.getNoteDisplayName("F", useSpanish)
        val toChord = ChordTransposer.getNoteDisplayName("C", useSpanish)

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
                        text = "Alterna limpiamente entre $fromChord (cejilla) y $toChord durante 1 minuto. Pulsa cada cambio limpio.",
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
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ── Racha Card (Matching Stitch Mockup) ──────────────────
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
                                val dayIso = dateSnapshot.weekIsoDates[idx]
                                val isDone = dayIso in completedDates
                                val isToday = idx == dateSnapshot.dayOfWeekIndex && !isDone
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

                // ── Rutina de Hoy (Inset Grouped Card) ───────────────────
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
                        // 1. Calentamiento Cromático
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

                        // 2. Práctica de Cambio de Acordes (NEW!)
                        val chordFrom = ChordTransposer.getNoteDisplayName("F", useSpanish)
                        val chordTo = ChordTransposer.getNoteDisplayName("C", useSpanish)
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

                        // 3. Reto de Entrenamiento Auditivo
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

                // ── El Acorde del Día Card (Kept & Styled) ───────────────
                state.chordOfTheDay?.let { info ->
                    val displayRoot = ChordTransposer.getNoteDisplayName(info.chord.root, useSpanish)
                    val fullChordName = "$displayRoot ${info.chord.type}"

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
                                        text = fullChordName,
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
                                ChordDiagramCanvas(chord = info.chord)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Fórmula: ${info.formula}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Escalas compatibles: ${info.compatibleScales.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (info.theoreticalExplanation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = info.theoreticalExplanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
