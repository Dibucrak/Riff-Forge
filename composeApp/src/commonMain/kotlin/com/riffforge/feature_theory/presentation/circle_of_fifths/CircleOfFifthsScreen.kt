package com.riffforge.feature_theory.presentation.circle_of_fifths

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.shared.domain.util.MusicTheoryData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val DIATONIC_ROMAN_NUMERALS = listOf("I", "ii", "iii", "IV", "V", "vi", "vii°")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleOfFifthsScreen(
    onNavigateUp: () -> Unit = {}
) {
    val useSpanish = AppSettings.useSpanishNotation
    val keys = MusicTheoryData.circleOfFifths
    var selectedKey by remember { mutableStateOf(keys.first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Círculo de Quintas", fontWeight = FontWeight.ExtraBold) },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            val selectedMajorRaw = selectedKey.majorName.substringBefore(" /").trim()
            val selectedMinorRaw = selectedKey.minorName.substringBefore(" /").trim()
            val selectedMajorDisplay = NotationConverter.formatChord(selectedMajorRaw, useSpanish)
            val selectedMinorDisplay = NotationConverter.formatChord(selectedMinorRaw, useSpanish)
            val selectedMajorFull = NotationConverter.formatChord(selectedKey.majorName, useSpanish)
            val selectedMinorFull = NotationConverter.formatChord(selectedKey.minorName, useSpanish)

            // ── Interactive Dual-Ring Wheel Card ─────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(306.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(292.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape)
                        )

                        Box(
                            modifier = Modifier
                                .size(196.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .size(98.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = selectedMajorDisplay,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = selectedMinorDisplay,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                maxLines = 1
                            )
                        }

                        val radiusOuter = 121f
                        val radiusInner = 74f

                        keys.forEachIndexed { index, keyInfo ->
                            val angleRad = ((index * 30.0) - 90.0) * PI / 180.0
                            val outerX = (cos(angleRad) * radiusOuter).dp
                            val outerY = (sin(angleRad) * radiusOuter).dp
                            val innerX = (cos(angleRad) * radiusInner).dp
                            val innerY = (sin(angleRad) * radiusInner).dp
                            val isSelected = selectedKey.majorName == keyInfo.majorName

                            val shortMajor = NotationConverter.formatChord(
                                keyInfo.majorName.substringBefore(" /").trim(),
                                useSpanish
                            )
                            val shortMinor = NotationConverter.formatChord(
                                keyInfo.minorName.substringBefore(" /").trim(),
                                useSpanish
                            )

                            Box(
                                modifier = Modifier
                                    .offset(x = outerX, y = outerY)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedKey = keyInfo },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shortMajor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (shortMajor.length > 2) 11.sp else 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .offset(x = innerX, y = innerY)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedKey = keyInfo },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shortMinor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    fontSize = if (shortMinor.length > 3) 9.sp else 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val accidentalsText = if (selectedKey.accidentals == "0") {
                        "0 alteraciones"
                    } else {
                        "${selectedKey.accidentals} alt."
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedMajorFull Mayor / $selectedMinorFull",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = accidentalsText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Acordes Diatónicos Card ──────────────────────────────────
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
                    Text(
                        text = "Acordes Diatónicos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val chords = selectedKey.chordsInKey
                    val row1 = chords.take(4)
                    val row2 = chords.drop(4)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row1.forEachIndexed { idx, chord ->
                            val roman = DIATONIC_ROMAN_NUMERALS.getOrElse(idx) { "" }
                            val displayChord = NotationConverter.formatChord(chord, useSpanish)
                            val isTonic = idx == 0
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isTonic) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primaryContainer
                                    )
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = displayChord,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    color = if (isTonic) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = roman,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isTonic) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (row2.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row2.forEachIndexed { idx, chord ->
                                val degreeIdx = idx + 4
                                val roman = DIATONIC_ROMAN_NUMERALS.getOrElse(degreeIdx) { "" }
                                val displayChord = NotationConverter.formatChord(chord, useSpanish)
                                val isDim = degreeIdx == 6

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isDim) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.primaryContainer,
                                    border = if (isDim) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = displayChord,
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = roman,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (row2.size < 4) {
                                Spacer(modifier = Modifier.weight((4 - row2.size).toFloat()))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
