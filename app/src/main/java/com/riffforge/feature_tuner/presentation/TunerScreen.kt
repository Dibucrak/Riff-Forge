package com.riffforge.feature_tuner.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.feature_songs.domain.util.ChordTransposer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class GuitarPeg(
    val stringNumber: Int,
    val name: String,
    val octave: Int,
    val targetFreq: Float
)

data class TuningPreset(
    val id: String,
    val label: String,
    val shortLabel: String,
    val isChromatic: Boolean = false,
    val pegs: List<GuitarPeg> = emptyList()
)

val TUNING_PRESETS = listOf(
    TuningPreset(
        id = "standard",
        label = "Estándar (EADGBE)",
        shortLabel = "Estándar EADGBE",
        pegs = listOf(
            GuitarPeg(6, "E", 2, 82.4f),
            GuitarPeg(5, "A", 2, 110.0f),
            GuitarPeg(4, "D", 3, 146.8f),
            GuitarPeg(3, "G", 3, 196.0f),
            GuitarPeg(2, "B", 3, 246.9f),
            GuitarPeg(1, "E", 4, 329.6f)
        )
    ),
    TuningPreset(
        id = "chromatic",
        label = "Cromático (Libre)",
        shortLabel = "Cromático Libre",
        isChromatic = true
    ),
    TuningPreset(
        id = "drop_d",
        label = "Drop D (DADGBE)",
        shortLabel = "Drop D",
        pegs = listOf(
            GuitarPeg(6, "D", 2, 73.4f),
            GuitarPeg(5, "A", 2, 110.0f),
            GuitarPeg(4, "D", 3, 146.8f),
            GuitarPeg(3, "G", 3, 196.0f),
            GuitarPeg(2, "B", 3, 246.9f),
            GuitarPeg(1, "E", 4, 329.6f)
        )
    ),
    TuningPreset(
        id = "half_step_down",
        label = "Half Step Down (Eb Ab Db Gb Bb Eb)",
        shortLabel = "Half Step Down (Eb)",
        pegs = listOf(
            GuitarPeg(6, "Eb", 2, 77.8f),
            GuitarPeg(5, "Ab", 2, 103.8f),
            GuitarPeg(4, "Db", 3, 138.6f),
            GuitarPeg(3, "Gb", 3, 185.0f),
            GuitarPeg(2, "Bb", 3, 233.1f),
            GuitarPeg(1, "Eb", 4, 311.1f)
        )
    ),
    TuningPreset(
        id = "full_step_down",
        label = "Full Step Down (DGCFAD)",
        shortLabel = "Full Step Down (D)",
        pegs = listOf(
            GuitarPeg(6, "D", 2, 73.4f),
            GuitarPeg(5, "G", 2, 98.0f),
            GuitarPeg(4, "C", 3, 130.8f),
            GuitarPeg(3, "F", 3, 174.6f),
            GuitarPeg(2, "A", 3, 220.0f),
            GuitarPeg(1, "D", 4, 293.7f)
        )
    ),
    TuningPreset(
        id = "drop_c",
        label = "Drop C (CGCFAD)",
        shortLabel = "Drop C",
        pegs = listOf(
            GuitarPeg(6, "C", 2, 65.4f),
            GuitarPeg(5, "G", 2, 98.0f),
            GuitarPeg(4, "C", 3, 130.8f),
            GuitarPeg(3, "F", 3, 174.6f),
            GuitarPeg(2, "A", 3, 220.0f),
            GuitarPeg(1, "D", 4, 293.7f)
        )
    ),
    TuningPreset(
        id = "open_g",
        label = "Open G (DGDGBD)",
        shortLabel = "Open G",
        pegs = listOf(
            GuitarPeg(6, "D", 2, 73.4f),
            GuitarPeg(5, "G", 2, 98.0f),
            GuitarPeg(4, "D", 3, 146.8f),
            GuitarPeg(3, "G", 3, 196.0f),
            GuitarPeg(2, "B", 3, 246.9f),
            GuitarPeg(1, "D", 4, 293.7f)
        )
    ),
    TuningPreset(
        id = "dadgad",
        label = "DADGAD (DADGAD)",
        shortLabel = "DADGAD",
        pegs = listOf(
            GuitarPeg(6, "D", 2, 73.4f),
            GuitarPeg(5, "A", 2, 110.0f),
            GuitarPeg(4, "D", 3, 146.8f),
            GuitarPeg(3, "G", 3, 196.0f),
            GuitarPeg(2, "A", 3, 220.0f),
            GuitarPeg(1, "D", 4, 293.7f)
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    isDarkTheme: Boolean,
    viewModel: TunerViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var selectedPreset by remember { mutableStateOf(TUNING_PRESETS.first()) }
    var tuningMenuExpanded by remember { mutableStateOf(false) }
    var selectedPegIndex by remember { mutableIntStateOf(0) }
    var isAutoMode by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val useSpanish = AppSettings.useSpanishNotation

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onEvent(TunerEvent.PermissionResult(isGranted))
    }

    LaunchedEffect(key1 = true) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    DisposableEffect(key1 = true) {
        onDispose {
            viewModel.onEvent(TunerEvent.StopListening)
        }
    }

    val isTuned = state.isListening && abs(state.centsOffset) <= 4f && state.currentNote != "-"
    val activeColor by animateColorAsState(
        targetValue = when {
            isTuned -> SystemGreen
            state.isListening && state.currentNote != "-" -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "TunerColorAnim"
    )

    val animatedCents by animateFloatAsState(
        targetValue = if (state.isListening && state.currentNote != "-") state.centsOffset.coerceIn(-50f, 50f) else 0f,
        animationSpec = spring(stiffness = 320f),
        label = "TunerCentsAnim"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Afinador Cromático",
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${selectedPreset.shortLabel} · 440 Hz",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Tuning Preset Dropdown ───────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { tuningMenuExpanded = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedPreset.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Cambiar afinación",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = tuningMenuExpanded,
                    onDismissRequest = { tuningMenuExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    TUNING_PRESETS.forEach { preset ->
                        val isCurrent = preset.id == selectedPreset.id
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.label,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (preset.isChromatic) {
                                        Text(
                                            text = "Libre",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedPreset = preset
                                selectedPegIndex = 0
                                tuningMenuExpanded = false
                                val targetFreq = if (isAutoMode || preset.isChromatic) null else preset.pegs.firstOrNull()?.targetFreq
                                viewModel.onEvent(TunerEvent.SetTargetFrequency(targetFreq))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── String Peg Selector (Hidden in Chromatic Mode) ───────────
            AnimatedVisibility(
                visible = !selectedPreset.isChromatic && selectedPreset.pegs.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Cuerdas",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isAutoMode) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.primary
                                        )
                                        .clickable {
                                            isAutoMode = !isAutoMode
                                            val targetFreq = if (isAutoMode) null else selectedPreset.pegs.getOrNull(selectedPegIndex)?.targetFreq
                                            viewModel.onEvent(TunerEvent.SetTargetFrequency(targetFreq))
                                        }
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isAutoMode) "Auto" else "Fija",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAutoMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                selectedPreset.pegs.forEachIndexed { index, peg ->
                                    val isSelected = selectedPegIndex == index
                                    val displayPeg = ChordTransposer.getNoteDisplayName(peg.name, useSpanish)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .clickable {
                                                selectedPegIndex = index
                                                isAutoMode = false
                                                viewModel.onEvent(TunerEvent.SetTargetFrequency(peg.targetFreq))
                                                com.riffforge.feature_ear_training.domain.util.AudioSynth.playReferenceTone(peg.targetFreq.toDouble())
                                            }
                                            .padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$displayPeg${peg.octave}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${peg.stringNumber}ª",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Pro Semicircular Arc Gauge Card ──────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status Pill
                    val centsInt = state.centsOffset.toInt()
                    val statusText = when {
                        !state.isListening -> "Micrófono en espera"
                        isTuned -> "Afinada · 0¢"
                        state.currentNote != "-" && centsInt < 0 -> "Baja · ${centsInt}¢"
                        state.currentNote != "-" && centsInt > 0 -> "Alta · +${centsInt}¢"
                        else -> "Toca una cuerda"
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isTuned) SystemGreen else MaterialTheme.colorScheme.surface,
                        border = if (isTuned) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isTuned) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (state.isListening) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isTuned) Color.Black.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Semicircular Arc Gauge Canvas
                    val arcTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    val needleColor = activeColor
                    val greenColor = SystemGreen

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.86f)
                                .height(160.dp)
                        ) {
                            val strokeW = 14.dp.toPx()
                            val arcDiameter = minOf(size.width, size.height * 1.9f) - strokeW
                            val arcRadius = arcDiameter / 2f
                            val center = Offset(size.width / 2f, size.height - 16.dp.toPx())
                            val topLeft = Offset(center.x - arcRadius, center.y - arcRadius)

                            // Background 180-degree arc
                            drawArc(
                                color = arcTrackColor,
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcDiameter, arcDiameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )

                            // Green target zone highlight at top center (262°..278°)
                            drawArc(
                                color = greenColor,
                                startAngle = 262f,
                                sweepAngle = 16f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcDiameter, arcDiameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )

                            // Tick marks (-50, -25, 0, +25, +50)
                            listOf(-50f, -25f, 0f, 25f, 50f).forEach { tickCents ->
                                val angleDeg = 270.0 + (tickCents / 50.0) * 90.0
                                val rad = angleDeg * PI / 180.0
                                val innerR = arcRadius - strokeW * 0.9f
                                val outerR = arcRadius + strokeW * 0.35f
                                drawLine(
                                    color = if (tickCents == 0f) greenColor else tickColor,
                                    start = Offset(
                                        center.x + (innerR * cos(rad)).toFloat(),
                                        center.y + (innerR * sin(rad)).toFloat()
                                    ),
                                    end = Offset(
                                        center.x + (outerR * cos(rad)).toFloat(),
                                        center.y + (outerR * sin(rad)).toFloat()
                                    ),
                                    strokeWidth = if (tickCents == 0f) 4.dp.toPx() else 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Sweeping Needle
                            val needleAngleDeg = 270.0 + (animatedCents / 50.0) * 86.0
                            val needleRad = needleAngleDeg * PI / 180.0
                            val needleLen = arcRadius + 4.dp.toPx()
                            drawLine(
                                color = needleColor,
                                start = center,
                                end = Offset(
                                    center.x + (needleLen * cos(needleRad)).toFloat(),
                                    center.y + (needleLen * sin(needleRad)).toFloat()
                                ),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Center Pivot Hub
                            drawCircle(
                                color = needleColor,
                                radius = 9.dp.toPx(),
                                center = center
                            )
                        }
                    }

                    // Scale Labels Row (-50¢ | 0¢ | +50¢)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.86f)
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "-50¢",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isTuned) SystemGreen else MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (state.isListening && state.currentNote != "-") {
                                    if (centsInt > 0) "+${centsInt}¢" else "${centsInt}¢"
                                } else "0¢",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isTuned) Color.Black.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "+50¢",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Bottom Note + Frequency Readout Row (Matching Stitch Pro)
                    val activePeg = selectedPreset.pegs.getOrNull(selectedPegIndex)
                    val rawNote = when {
                        state.currentNote != "-" -> state.currentNote
                        !selectedPreset.isChromatic && activePeg != null -> "${activePeg.name}${activePeg.octave}"
                        else -> "--"
                    }
                    val displayNote = if (rawNote == "--") "--" else {
                        ChordTransposer.getNoteDisplayName(rawNote, useSpanish)
                    }

                    val freqDisplay = when {
                        state.currentFrequency > 0f -> "${String.format("%.1f", state.currentFrequency)} Hz"
                        !selectedPreset.isChromatic && activePeg != null -> "${String.format("%.1f", activePeg.targetFreq)} Hz"
                        else -> "-- Hz"
                    }
                    val freqSubtitle = when {
                        state.currentFrequency > 0f -> "Frecuencia detectada"
                        !selectedPreset.isChromatic -> "Frecuencia objetivo"
                        else -> "Modo cromático libre"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(min = 96.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isTuned) SystemGreen
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayNote,
                                fontSize = if (displayNote.length > 3) 26.sp else 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isTuned) Color.Black.copy(alpha = 0.9f) else activeColor
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = freqDisplay,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = freqSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Primary Action Button (50dp) ─────────────────────────────
            if (!state.hasPermission) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Otorgar Permiso de Micrófono", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (state.isListening) {
                            viewModel.onEvent(TunerEvent.StopListening)
                        } else {
                            viewModel.onEvent(TunerEvent.StartListening)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (state.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isListening) "Detener Afinador" else "Iniciar Afinador",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.navigationBarsPadding().height(104.dp))
        }
    }
}
