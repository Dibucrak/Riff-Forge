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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import com.riffforge.feature_theory.domain.util.ProgressionLibrary

@Composable
fun ProgressionScreen(
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: ProgressionViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
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
                        text = "Generador de Progresiones",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Content area - below header (solid, nothing overlaps)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(ProgressionLibrary.genres) { genre ->
                        val isSelected = state.selectedGenre == genre
                        val bg = if (isSelected) AccentTint else (if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim)
                        val textC = if (isSelected) OnAccent else dimTextColor
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bg)
                                .clickable { viewModel.onEvent(ProgressionEvent.SelectGenre(genre)) }
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = genre, fontWeight = FontWeight.Bold, color = textC)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.displayedProgressions.forEach { progression ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            isDarkTheme = isDarkTheme,
                            cornerRadius = 24.dp
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
                                color = dimTextColor
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = (if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Análisis Armónico:",
                                style = MaterialTheme.typography.labelLarge,
                                color = textColor,
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
                                color = textColor,
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}