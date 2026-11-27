package com.riffforge.feature_tuner.presentation

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riffforge.core.audio.AudioSynth
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.feature_tuner.domain.observeMicrophonePitch
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class GuitarPeg(
    val stringNumber: Int,
    val noteName: String,
    val octave: Int,
    val targetFreq: Float
)

private data class TuningPreset(
    val id: String,
    val label: String,
    val shortLabel: String,
    val isChromatic: Boolean = false,
    val pegs: List<GuitarPeg> = emptyList()
)

private val TUNING_PRESETS = listOf(
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
    isDarkTheme: Boolean = false
) {
    val useSpanish = AppSettings.useSpanishNotation
    var selectedPreset by remember { mutableStateOf(TUNING_PRESETS.first()) }
    var tuningMenuExpanded by remember { mutableStateOf(false) }
    var selectedPegIndex by remember { mutableStateOf(0) }
    var isAutoMode by remember { mutableStateOf(true) }
    var isListening by remember { mutableStateOf(false) }
    var micUnavailable by remember { mutableStateOf(false) }
    var noteName by remember { mutableStateOf("--") }
    var centsDeviation by remember { mutableStateOf(0f) }
    var currentFrequency by remember { mutableStateOf(0f) }

    val activePeg = selectedPreset.pegs.getOrNull(selectedPegIndex)
    val latestTargetFreq by rememberUpdatedState(
        if (isAutoMode || selectedPreset.isChromatic) null else activePeg?.targetFreq
    )

    LaunchedEffect(isListening) {
        if (isListening) {
            micUnavailable = false
            var receivedAnySample = false
            observeMicrophonePitch { latestTargetFreq }.collectLatest { pitch ->
                receivedAnySample = true
                if (pitch != null) {
                    noteName = "${pitch.noteName}${pitch.octave}"
                    centsDeviation = pitch.cents
                    currentFrequency = pitch.frequency
                }
            }
            // Flow completed (mic permission denied or unavailable audio input)
            if (isListening && !receivedAnySample) {
                micUnavailable = true
                isListening = false
            }
        } else {
            noteName = "--"
            centsDeviation = 0f
            currentFrequency = 0f
        }
    }

    val isTuned = isListening && abs(centsDeviation) <= 4f && noteName != "--"
    val activeColor by animateColorAsState(
        targetValue = when {
            isTuned -> SystemGreen
            isListening && noteName != "--" -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200)
    )

    val animatedCents by animateFloatAsState(
        targetValue = if (isListening && noteName != "--") centsDeviation.coerceIn(-50f, 50f) else 0f,
        animationSpec = spring(stiffness = 320f)
    )

    val scrollState = rememberScrollState()

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
                .background(MaterialTheme.colorScheme.background)
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
                                        .clickable { isAutoMode = !isAutoMode }
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
                                    val displayPeg = NotationConverter.formatChord(peg.noteName, useSpanish)
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
                                                AudioSynth.playTone(peg.targetFreq.toDouble(), durationMs = 420)
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
                    val centsInt = centsDeviation.roundToInt()
                    val statusText = when {
                        micUnavailable -> "Micrófono sin permiso o no disponible"
                        !isListening -> "Micrófono en espera"
                        isTuned -> "Afinada · 0¢"
                        noteName != "--" && centsInt < 0 -> "Baja · ${centsInt}¢"
                        noteName != "--" && centsInt > 0 -> "Alta · +${centsInt}¢"
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
                                            if (isListening) MaterialTheme.colorScheme.primary
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

                            drawArc(
                                color = arcTrackColor,
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcDiameter, arcDiameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )

                            drawArc(
                                color = greenColor,
                                startAngle = 262f,
                                sweepAngle = 16f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcDiameter, arcDiameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )

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

                            drawCircle(
                                color = needleColor,
                                radius = 9.dp.toPx(),
                                center = center
                            )
                        }
                    }

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
                                text = if (isListening && noteName != "--") {
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

                    val rawNote = when {
                        noteName != "--" -> noteName
                        !selectedPreset.isChromatic && activePeg != null -> "${activePeg.noteName}${activePeg.octave}"
                        else -> "--"
                    }
                    val displayNote = if (rawNote == "--") "--" else NotationConverter.formatChord(rawNote, useSpanish)
                    val shownFreq = when {
                        currentFrequency > 0f -> ((currentFrequency * 10).roundToInt() / 10.0).toString() + " Hz"
                        !selectedPreset.isChromatic && activePeg != null -> "${activePeg.targetFreq} Hz"
                        else -> "-- Hz"
                    }
                    val freqSubtitle = when {
                        currentFrequency > 0f -> "Frecuencia detectada"
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
                                .background(if (isTuned) SystemGreen else MaterialTheme.colorScheme.surface)
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
                                text = shownFreq,
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

            Button(
                onClick = { isListening = !isListening },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isListening) "Detener Afinador" else "Iniciar Afinador",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.navigationBarsPadding().height(104.dp))
        }
    }
}
