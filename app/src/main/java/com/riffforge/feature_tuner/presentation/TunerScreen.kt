package com.riffforge.feature_tuner.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.components.*
import com.riffforge.core.presentation.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Liquid Glass Tuner Screen — glassmorphic panel with frosted glass card,
 * gradient edge highlights, and soft colored shadows over the vibrant background.
 */
@Composable
fun TunerScreen(
    isDarkTheme: Boolean,
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
        targetValue = if (isTuned) SystemGreen else if (state.currentNote != "-") SystemRed else (if (isDarkTheme) TextOnGlassDimDark else TextOnGlassDimLight),
        animationSpec = tween(durationMillis = 300),
        label = "LedColorAnimation"
    )

    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!state.hasPermission) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkTheme = isDarkTheme,
                cornerRadius = 24.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Se requiere acceso al micrófono.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassButtonAccent(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    ) {
                        Text("Otorgar Permiso", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            GlassCardElevated(
                modifier = Modifier.fillMaxWidth(),
                isDarkTheme = isDarkTheme,
                cornerRadius = 32.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Status indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim,
                                    RoundedCornerShape(50.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (state.isListening) SystemGreen else SystemRed,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isListening) "Escuchando..." else "Detenido",
                                style = MaterialTheme.typography.labelMedium,
                                color = dimTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TunerDialLED(
                        centsOffset = state.centsOffset,
                        activeColor = activeColor,
                        isDarkTheme = isDarkTheme
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Note display
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight,
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.currentNote,
                            fontSize = 80.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (state.currentFrequency > 0) "${String.format("%.1f", state.currentFrequency)} Hz" else "-- Hz",
                        style = MaterialTheme.typography.headlineSmall,
                        color = dimTextColor,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mic toggle button
                    GlassButtonAccent(
                        onClick = {
                            if (state.isListening) {
                                viewModel.onEvent(TunerEvent.StopListening)
                            } else {
                                viewModel.onEvent(TunerEvent.StartListening)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (state.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Alternar micrófono",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isListening) "Detener" else "Iniciar",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TunerDialLED(centsOffset: Float, activeColor: Color, isDarkTheme: Boolean) {
    val trackColor = if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim
    val tickCenterColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val tickColor = if (isDarkTheme) TextOnGlassDimDark else TextOnGlassDimLight
    val centerBg = if (isDarkTheme) SecondarySystemBackgroundDark else SecondarySystemGroupedBackgroundLight

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
                color = trackColor,
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
                val tickC = if (isCenter) tickCenterColor else tickColor

                val startX = center.x + (radius - 40f) * cos(angleRad).toFloat()
                val startY = center.y + (radius - 40f) * sin(angleRad).toFloat()

                val endX = center.x + (radius - 40f - tickLength) * cos(angleRad).toFloat()
                val endY = center.y + (radius - 40f - tickLength) * sin(angleRad).toFloat()

                drawLine(
                    color = tickC,
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
                color = centerBg,
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