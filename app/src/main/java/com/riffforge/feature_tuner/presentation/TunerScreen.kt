package com.riffforge.feature_tuner.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    viewModel: TunerViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Afinador") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (state.isListening) {
                        viewModel.onEvent(TunerEvent.StopListening)
                    } else {
                        if (state.hasPermission) {
                            viewModel.onEvent(TunerEvent.StartListening)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                containerColor = if (state.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (state.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Alternar micrófono"
                )
            }
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
            if (!state.hasPermission) {
                Text(
                    text = "Se requiere acceso al micrófono para usar el afinador.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                    Text("Otorgar Permiso")
                }
            } else {
                TunerDial(centsOffset = state.centsOffset)

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = state.currentNote,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${String.format("%.2f", state.currentFrequency)} Hz",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                val tuneStatus = when {
                    state.centsOffset < -5f -> "Muy grave"
                    state.centsOffset > 5f -> "Muy agudo"
                    state.currentNote != "-" -> "¡Afinado!"
                    else -> "Esperando sonido..."
                }

                Text(
                    text = tuneStatus,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (tuneStatus == "¡Afinado!") Color.Green else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun TunerDial(centsOffset: Float) {
    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.width / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawArc(
                color = Color.LightGray,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 8f)
            )

            drawLine(
                color = Color.Green,
                start = Offset(center.x, center.y - radius + 10f),
                end = Offset(center.x, center.y - radius - 20f),
                strokeWidth = 6f
            )

            val angleOffset = (centsOffset / 50f) * 90f
            val needleAngle = 270f + angleOffset

            val angleRad = Math.toRadians(needleAngle.toDouble())
            val needleEndX = center.x + (radius - 30f) * cos(angleRad).toFloat()
            val needleEndY = center.y + (radius - 30f) * sin(angleRad).toFloat()

            drawLine(
                color = if (abs(centsOffset) <= 5f) Color.Green else Color.Red,
                start = center,
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color.DarkGray,
                radius = 15f,
                center = center
            )
        }
    }
}