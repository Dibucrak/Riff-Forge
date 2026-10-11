package com.riffforge.feature_metronome.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeScreen(
    onNavigateUp: () -> Unit
) {
    var bpm by remember { mutableIntStateOf(120) }
    var isPlaying by remember { mutableStateOf(false) }
    var beatsPerMeasure by remember { mutableIntStateOf(4) }
    var currentBeat by remember { mutableIntStateOf(0) }

    // Use rememberUpdatedState to always read the latest values inside the coroutine
    val currentIsPlaying by rememberUpdatedState(isPlaying)
    val currentBpm by rememberUpdatedState(bpm)
    val currentBeatsPerMeasure by rememberUpdatedState(beatsPerMeasure)

    // Metronome timer logic - launch when isPlaying changes
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val intervalMs = (60000L / currentBpm)
            currentBeat = 1
            // Play the first beat immediately
            playMetronomeClick(true, 1)
            // Wait for subsequent beats
            delay(intervalMs)
            while (isActive && currentIsPlaying) {
                currentBeat = if (currentBeat >= currentBeatsPerMeasure) 1 else currentBeat + 1
                playMetronomeClick(currentBeat == 1, currentBeat)
                delay(intervalMs)
            }
            currentBeat = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metrónomo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..beatsPerMeasure) {
                        val isActive = i == currentBeat && isPlaying
                        val isFirstBeat = i == 1

                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(if (isActive) 32.dp else 24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isActive && isFirstBeat -> MaterialTheme.colorScheme.primary
                                        isActive -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = bpm.toString(),
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { if (bpm > 40) bpm-- },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Reducir BPM")
                    }

                    Slider(
                        value = bpm.toFloat(),
                        onValueChange = { bpm = it.toInt() },
                        valueRange = 40f..240f,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = { if (bpm < 240) bpm++ },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Aumentar BPM")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val timeSignatures = listOf(2, 3, 4, 6)
                    timeSignatures.forEach { beats ->
                        SuggestionChip(
                            onClick = { beatsPerMeasure = beats },
                            label = { Text("$beats/4") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (beatsPerMeasure == beats) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                FloatingActionButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.size(80.dp),
                    containerColor = if (isPlaying) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Reproducir/Pausar",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

// Platform-agnostic metronome click using expect/actual
expect fun playMetronomeClick(isAccent: Boolean, beat: Int)
