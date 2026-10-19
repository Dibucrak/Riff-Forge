package com.riffforge.feature_theory.presentation.progressions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicVideo
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
import com.riffforge.feature_theory.domain.util.ProgressionLibrary

/**
 * Liquid Glass Progression Screen — chord progressions with frosted glass aesthetic.
 *
 * Visual design:
 *   - Full-screen vibrant gradient background with ambient glow orbs
 *   - Frosted glass header bar with back button
 *   - Genre selectors use glass chips with accent colors
 *   - Each progression card uses elevated glass panel
 *   - Chord chips use purple-tinted glass surfaces
 */
@Composable
fun ProgressionScreen(
    onNavigateUp: () -> Unit
) {
    var selectedGenre by remember { mutableStateOf("Rock") }

    val displayedProgressions = ProgressionLibrary.progressions.filter { it.genre == selectedGenre }

    GlassBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header Bar ───────────────────────────────────────────
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
                        text = "Generador de Progresiones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextOnGlassPrimary
                    )
                }
            }

            // ── Genre Selector ───────────────────────────────────────
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(ProgressionLibrary.genres) { genre ->
                    val isSelected = selectedGenre == genre
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) AccentTint else GlassSurfaceDim)
                            .clickable { selectedGenre = genre }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = genre,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) OnAccent else TextOnGlassSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Progressions List ────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayedProgressions) { progression ->
                    GlassCardElevated(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = progression.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentTint
                                )
                                Icon(
                                    imageVector = Icons.Default.MusicVideo,
                                    contentDescription = null,
                                    tint = AccentTint
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = progression.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnGlassSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                color = TextOnGlassDim.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Análisis Armónico:",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextOnGlassPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = progression.romanNumerals,
                                style = MaterialTheme.typography.headlineSmall,
                                color = AccentTint,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Text(
                                text = "Acordes de Ejemplo:",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextOnGlassPrimary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                progression.chords.forEach { chord ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(AccentTint.copy(alpha = 0.25f))
                                            .clickable { }
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = chord,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            color = TextOnGlassPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
