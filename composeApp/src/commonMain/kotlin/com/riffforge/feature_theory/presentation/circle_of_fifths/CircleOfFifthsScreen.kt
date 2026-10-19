package com.riffforge.feature_theory.presentation.circle_of_fifths

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_theory.domain.model.KeySignature
import com.riffforge.feature_theory.domain.util.MusicTheoryData

/**
 * Liquid Glass Circle of Fifths Screen — with frosted glass aesthetic.
 *
 * Visual design:
 *   - Full-screen vibrant gradient background with ambient glow orbs
 *   - Frosted glass header bar with back button
 *   - Key info card uses elevated glass panel
 *   - Circle nodes use accent colors (cyan/purple) instead of Material colors
 *   - Chord chips use glass stat chips
 */
@Composable
fun CircleOfFifthsScreen(
    onNavigateUp: () -> Unit
) {
    val keys = MusicTheoryData.circleOfFifths
    var selectedKey by remember { mutableStateOf(keys[0]) }

    GlassBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header Bar ───────────────────────────────────────────
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateUp,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = AccentTint.copy(alpha = 0.3f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Regresar",
                                tint = AccentTint
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Círculo de Quintas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextOnGlassPrimary
                        )
                    }
                }
            }

            // Chord info card
            item {
                GlassCardElevated(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tonalidad: ${selectedKey.majorName} / ${selectedKey.minorName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextOnGlassPrimary
                            )
                            Text(
                                text = "Alt: ${selectedKey.accidentals}",
                                style = MaterialTheme.typography.titleSmall,
                                color = AccentTint
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Acordes en la escala:",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextOnGlassSecondary
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
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(AccentTint.copy(alpha = 0.2f))
                                            .clickable { }
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = chord,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            color = TextOnGlassPrimary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Circle visualization
            item {
                val circleSize = 300.dp
                val radiusOuter = 120.dp
                val radiusInner = 75.dp
                val outerNodeSize = 48.dp
                val innerNodeSize = 40.dp

                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    keys.forEachIndexed { index, keySignature ->
                        val angleDeg = (index * 30.0) - 90.0
                        val angleRad = angleDeg * kotlin.math.PI / 180.0

                        val outerX = (kotlin.math.cos(angleRad) * radiusOuter.value).dp
                        val outerY = (kotlin.math.sin(angleRad) * radiusOuter.value).dp

                        val innerX = (kotlin.math.cos(angleRad) * radiusInner.value).dp
                        val innerY = (kotlin.math.sin(angleRad) * radiusInner.value).dp

                        val isSelected = selectedKey.id == keySignature.id

                        Box(
                            modifier = Modifier
                                .size(outerNodeSize)
                                .align(Alignment.Center)
                                .offset(x = outerX, y = outerY)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentTint else GlassSurfaceDim)
                                .clickable { selectedKey = keySignature },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = keySignature.majorName,
                                color = if (isSelected) OnAccent else TextOnGlassSecondary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(innerNodeSize)
                                .align(Alignment.Center)
                                .offset(x = innerX, y = innerY)
                                .clip(CircleShape)
                                .background(if (isSelected) AccentTint else GlassSurfaceDim)
                                .clickable { selectedKey = keySignature },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = keySignature.minorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) OnAccent else TextOnGlassDim,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
