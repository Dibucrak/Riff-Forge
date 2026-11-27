package com.riffforge.feature_splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riffforge.core.presentation.theme.AccentTint
import com.riffforge.core.presentation.theme.GlassBackgroundBase
import com.riffforge.core.presentation.theme.TextOnGlassPrimary
import com.riffforge.core.presentation.theme.TextOnGlassSecondary
import kotlinx.coroutines.delay

/**
 * Liquid Glass Splash Screen — Apple HIG branded launch experience.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val accentColor = AccentTint
    val backgroundColor = GlassBackgroundBase
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scale)
        ) {
            // Elegant Frosted Glass Emblem with Specular Border
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(accentColor.copy(alpha = 0.14f), CircleShape)
                    .border(1.5.dp, accentColor.copy(alpha = 0.40f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Riff Forge",
                    tint = accentColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App name in Apple HIG Typography
            Text(
                text = "Riff Forge",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnGlassPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Estudio & Teoría para Guitarristas",
                fontSize = 14.sp,
                color = TextOnGlassSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
