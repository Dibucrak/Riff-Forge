package com.riffforge.feature_tuner.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val isTuned = abs(state.centsOffset) <= 5f && state.currentNote != "-"
    val activeColor by animateColorAsState(
        targetValue = if (isTuned) Color(0xFF00FF00) else if (state.currentNote != "-") Color(0xFFFF3B30) else Color.DarkGray,
        animationSpec = tween(durationMillis = 300),
        label = "LedColorAnimation"
    )

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (!state.hasPermission) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Se requiere acceso al micrófono.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                        Text("Otorgar Permiso")
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        TunerDialLED(centsOffset = state.centsOffset, activeColor = activeColor)

                        Spacer(modifier = Modifier.height(32.dp))

                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.currentNote,
                                fontSize = 80.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (state.currentFrequency > 0) "${String.format("%.1f", state.currentFrequency)} Hz" else "-- Hz",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.LightGray,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val tuneStatus = when {
                            state.centsOffset < -5f -> "Sube la afinación"
                            state.centsOffset > 5f -> "Baja la afinación"
                            state.currentNote != "-" -> "Perfecto"
                            else -> "Toca una cuerda"
                        }

                        Text(
                            text = tuneStatus.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = activeColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TunerDialLED(centsOffset: Float, activeColor: Color) {
    Box(
        modifier = Modifier
            .size(260.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.width / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawArc(
                color = Color(0xFF2A2A2A),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = 24f, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )

            for (i in -5..5) {
                val tickAngle = 270f + (i * 18f)
                val angleRad = Math.toRadians(tickAngle.toDouble())

                val isCenter = i == 0
                val tickLength = if (isCenter) 30f else 15f
                val tickThickness = if (isCenter) 6f else 3f
                val tickColor = if (isCenter) Color.White else Color.Gray

                val startX = center.x + (radius - 40f) * cos(angleRad).toFloat()
                val startY = center.y + (radius - 40f) * sin(angleRad).toFloat()

                val endX = center.x + (radius - 40f - tickLength) * cos(angleRad).toFloat()
                val endY = center.y + (radius - 40f - tickLength) * sin(angleRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickThickness,
                    cap = StrokeCap.Round
                )
            }

            val clampedCents = centsOffset.coerceIn(-50f, 50f)
            val angleOffset = (clampedCents / 50f) * 90f
            val needleAngle = 270f + angleOffset
            val needleRad = Math.toRadians(needleAngle.toDouble())

            val needleEndX = center.x + (radius - 20f) * cos(needleRad).toFloat()
            val needleEndY = center.y + (radius - 20f) * sin(needleRad).toFloat()

            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(activeColor.copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(needleEndX, needleEndY),
                    radius = 40f
                ),
                start = center,
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 30f,
                cap = StrokeCap.Round
            )

            drawLine(
                color = activeColor,
                start = center,
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color(0xFF121212),
                radius = 20f,
                center = center
            )
            drawCircle(
                color = activeColor,
                radius = 10f,
                center = center
            )
        }
    }
}