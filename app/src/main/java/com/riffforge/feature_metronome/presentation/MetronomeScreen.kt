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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeScreen(
    onNavigateUp: () -> Unit,
    viewModel: MetronomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    DisposableEffect(key1 = true) {
        onDispose {
            if (viewModel.state.value.isPlaying) {
                viewModel.onEvent(MetronomeEvent.TogglePlayPause)
            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..state.beatsPerMeasure) {
                    val isActive = i == state.currentBeat && state.isPlaying
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
                text = state.bpm.toString(),
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
                    onClick = { if (state.bpm > 40) viewModel.onEvent(MetronomeEvent.ChangeBpm(state.bpm - 1)) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Reducir BPM")
                }

                Slider(
                    value = state.bpm.toFloat(),
                    onValueChange = { viewModel.onEvent(MetronomeEvent.ChangeBpm(it.toInt())) },
                    valueRange = 40f..240f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )

                IconButton(
                    onClick = { if (state.bpm < 240) viewModel.onEvent(MetronomeEvent.ChangeBpm(state.bpm + 1)) },
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
                        onClick = { viewModel.onEvent(MetronomeEvent.ChangeBeatsPerMeasure(beats)) },
                        label = { Text("$beats/4") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (state.beatsPerMeasure == beats) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            FloatingActionButton(
                onClick = { viewModel.onEvent(MetronomeEvent.TogglePlayPause) },
                modifier = Modifier.size(80.dp),
                containerColor = if (state.isPlaying) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir/Pausar",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}