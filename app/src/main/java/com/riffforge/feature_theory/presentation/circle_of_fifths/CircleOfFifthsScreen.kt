package com.riffforge.feature_theory.presentation.circle_of_fifths

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_theory.domain.model.KeySignature
import com.riffforge.feature_theory.domain.util.MusicTheoryData
import kotlin.math.cos
import kotlin.math.sin

/**
 * Liquid Glass Circle of Fifths — glassmorphic info card with cyan/purple nodes
 * and chord chips with purple glass backgrounds.
 */
@Composable
fun CircleOfFifthsScreen(
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean
) {
    val keys = MusicTheoryData.circleOfFifths
    var selectedKey by remember { mutableStateOf(keys[0]) }
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight
    val glassSurface = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight

    GlassBackground(isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top bar
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                isDarkTheme = isDarkTheme,
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateUp,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = glassSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = textColor)
                    }
                    Text(
                        text = "Círculo de Quintas",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circle visualization
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val radiusOuter = 130.dp
                    val radiusInner = 80.dp

                    keys.forEachIndexed { index, keySignature ->
                        val angleStr = Math.toRadians((index * 30.0) - 90.0)
                        val outerX = (cos(angleStr) * radiusOuter.value).dp
                        val outerY = (sin(angleStr) * radiusOuter.value).dp
                        val innerX = (cos(angleStr) * radiusInner.value).dp
                        val innerY = (sin(angleStr) * radiusInner.value).dp
                        val isSelected = selectedKey.id == keySignature.id

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                                .offset(x = outerX, y = outerY)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentTint else (if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim))
                                .clickable { selectedKey = keySignature },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = keySignature.majorName,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = if (isSelected) OnAccent else textColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                                .offset(x = innerX, y = innerY)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentTint else Color.Transparent)
                                .clickable { selectedKey = keySignature },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = keySignature.minorName,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) OnAccent else dimTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDarkTheme = isDarkTheme,
                    cornerRadius = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tonalidad: ${selectedKey.majorName} / ${selectedKey.minorName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Alt: ${selectedKey.accidentals}",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentTint
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Acordes en la escala:",
                        style = MaterialTheme.typography.labelLarge,
                        color = dimTextColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val chunkedChords = selectedKey.chordsInKey.chunked(4)
                    chunkedChords.forEach { rowChords ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowChords.forEach { chord ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            AccentTint.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chord,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        color = textColor,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}