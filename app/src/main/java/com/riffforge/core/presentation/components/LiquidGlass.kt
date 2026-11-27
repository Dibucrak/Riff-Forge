package com.riffforge.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.riffforge.core.presentation.theme.GlassAmbientLightDark
import com.riffforge.core.presentation.theme.GlassAmbientLightLight
import com.riffforge.core.presentation.theme.GlassBackgroundBaseDark
import com.riffforge.core.presentation.theme.GlassBackgroundBaseLight
import com.riffforge.core.presentation.theme.TextOnGlassPrimaryDark
import com.riffforge.core.presentation.theme.TextOnGlassPrimaryLight

/**
 * Liquid Glass Background — neutral system canvas (black / #F2F2F7) with a single, very soft
 * warm ambient light near the top so the translucent glass has something to refract.
 */
@Composable
fun GlassBackground(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val baseColor = if (isDarkTheme) GlassBackgroundBaseDark else GlassBackgroundBaseLight
    val ambient = if (isDarkTheme) GlassAmbientLightDark else GlassAmbientLightLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseColor)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-160).dp)
                .size(420.dp)
                .background(
                    Brush.radialGradient(colors = listOf(ambient, Color.Transparent)),
                    shape = CircleShape
                )
        )
        CompositionLocalProvider(
            LocalContentColor provides if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
        ) {
            content()
        }
    }
}
