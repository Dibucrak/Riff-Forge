package com.riffforge.feature_home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.riffforge.LocalAuthManager
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.navigation.RiffForgeTab
import com.riffforge.core.presentation.theme.*
import com.riffforge.core.domain.store.RepertoireStore
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.feature_songs.presentation.AddEditSongToolScreen
import com.riffforge.feature_songs.presentation.SongViewerToolScreen
import com.riffforge.feature_tools.presentation.ChordDictionaryToolScreen
import com.riffforge.feature_tools.presentation.MetronomeToolScreen
import com.riffforge.feature_tools.presentation.ScalesToolScreen

// ── Data Models ────────────────────────────────────────────────────────────

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

data class RecentRiffItem(
    val id: Int,
    val title: String,
    val key: String,
    val tempo: Int,
    val lastPlayed: String
)

// ── Screen ─────────────────────────────────────────────────────────────────

/**
 * Liquid Glass Home Screen — Apple Human Interface Guidelines compliant dashboard.
 *
 * Design features:
 *   - Edge-to-edge layout with safe margins.
 *   - Native Large Title hierarchy with contextual greeting.
 *   - Inset Grouped cards for recent riffs with hairline separators.
 *   - Refined metric chips with semantic color hierarchy.
 *   - Natural Spanish musical copy ("Reproducir", "Practicar", not "Jugar").
 */
@Composable
fun HomeScreen() {
    val navigator = LocalNavigator.current
    val tabNavigator = LocalTabNavigator.current
    val authManager = LocalAuthManager.current
    val currentUser by authManager.currentUser.collectAsState(initial = null)

    val allSongs = RepertoireStore.songs
    val featuredSong = allSongs.firstOrNull()
    val recentSongs = remember(allSongs.toList()) { allSongs.take(4) }
    val useSpanish = AppSettings.useSpanishNotation

    val openSongViewer: (String) -> Unit = { targetSongId ->
        navigator?.push(
            SongViewerToolScreen(
                songId = targetSongId,
                setlistId = null
            )
        )
    }

    val quickActions = listOf(
        QuickActionItem("Afinador", "Precisión cromática", Icons.Default.Mic, AccentTint) {
            tabNavigator.current = RiffForgeTab.Tuner
        },
        QuickActionItem("Metrónomo", "Tempo y compás", Icons.Default.Timer, SystemYellow) {
            navigator?.let { it.push(MetronomeToolScreen(it)) }
        },
        QuickActionItem("Escalas", "Mástil y modos", Icons.Default.MusicNote, SystemGreen) {
            navigator?.let { it.push(ScalesToolScreen(it)) }
        },
        QuickActionItem("Acordes", "Diccionario y formas", Icons.Default.Book, AccentTint) {
            navigator?.let { it.push(ChordDictionaryToolScreen(it)) }
        }
    )

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // ── Top Header ─────────────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Panel Principal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentTint,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Riff Forge",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextOnGlassPrimary
                    )
                }

                // Apple-style circular avatar button
                GlassIconButton(
                    onClick = { tabNavigator.current = RiffForgeTab.Profile },
                    size = 42.dp
                ) {
                    ProfileAvatar(
                        photoData = currentUser?.photoData.orEmpty(),
                        photoUrl = currentUser?.photoUrl.orEmpty(),
                        size = 38.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Hero Featured Card ("Continuar práctica") ──────────────
            val heroTitle = featuredSong?.title ?: "Comienza tu repertorio"
            val heroKey = featuredSong?.let { NotationConverter.formatChord(it.key, useSpanish) } ?: "C"
            val heroBpm = featuredSong?.let { "${it.bpm} BPM" } ?: "Práctica"
            val heroArtist = featuredSong?.artist ?: "Toca para agregar"

            GlassCardElevated(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (featuredSong != null) {
                        openSongViewer(featuredSong.id)
                    } else {
                        navigator?.push(AddEditSongToolScreen(songId = null))
                    }
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(SystemGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Continuar práctica",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextOnGlassSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = heroTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextOnGlassPrimary,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassBadge(text = heroKey, tintColor = AccentTint)
                            GlassBadge(text = heroBpm, tintColor = SystemYellow)
                            GlassBadge(text = heroArtist, tintColor = TextOnGlassSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    GlassButtonAccent(
                        onClick = {
                            if (featuredSong != null) {
                                openSongViewer(featuredSong.id)
                            } else {
                                navigator?.push(AddEditSongToolScreen(songId = null))
                            }
                        },
                        cornerRadius = 14.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Reproducir",
                            tint = OnAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (featuredSong != null) "Tocar" else "Crear",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Quick Metrics Row ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Riffs",
                    value = "12",
                    accentColor = AccentTint
                )
                GlassStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Práctica",
                    value = "3.5h",
                    accentColor = SystemGreen
                )
                GlassStatChip(
                    modifier = Modifier.weight(1f),
                    label = "Tonalidad",
                    value = "Am",
                    accentColor = SystemYellow
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Quick Actions Grid ─────────────────────────────────────
            GlassSectionHeader(
                title = "Acciones Rápidas",
                subtitle = "Herramientas de uso frecuente"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quickActions.take(2).forEach { action ->
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 16.dp,
                        onClick = action.onClick
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(action.accentColor.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = action.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextOnGlassPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = action.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextOnGlassSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quickActions.drop(2).forEach { action ->
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 16.dp,
                        onClick = action.onClick
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(action.accentColor.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = action.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextOnGlassPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = action.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextOnGlassSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Recent Riffs (Apple Inset Grouped Section) ─────────────
            GlassSectionHeader(
                title = "Riffs Recientes",
                subtitle = "Tu colección de práctica",
                actionText = "Ver todos",
                onActionClick = { tabNavigator.current = RiffForgeTab.Songs }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (recentSongs.isEmpty()) {
                GlassCardElevated(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { tabNavigator.current = RiffForgeTab.Songs }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tienes canciones en tu repertorio aún",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnGlassSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        GlassButtonAccent(
                            onClick = { navigator?.push(AddEditSongToolScreen(songId = null)) },
                            cornerRadius = 12.dp
                        ) {
                            Text("Agregar Canción", fontWeight = FontWeight.Bold, color = OnAccent)
                        }
                    }
                }
            } else {
                GlassInsetGroup {
                    recentSongs.forEachIndexed { index, song ->
                        val displayKey = NotationConverter.formatChord(song.key, useSpanish)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { openSongViewer(song.id) }
                                )
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GlassSurfaceDim, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = AccentTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextOnGlassPrimary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$displayKey · ${song.bpm} BPM",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOnGlassSecondary
                                        )
                                        Text(
                                            text = "·",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOnGlassDim
                                        )
                                        Text(
                                            text = song.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOnGlassDim,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            GlassIconButton(
                                onClick = { openSongViewer(song.id) },
                                size = 32.dp,
                                backgroundColor = AccentTint.copy(alpha = 0.12f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir",
                                    tint = AccentTint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (index < recentSongs.lastIndex) {
                            GlassSeparator(startIndent = 64.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
