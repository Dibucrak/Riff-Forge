package com.riffforge.core.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.riffforge.core.presentation.theme.*

/*
 * Liquid Glass components — following Apple's Human Interface Guidelines for materials.
 *
 *   - Glass is a neutral, translucent material: it never carries brand color itself.
 *   - A specular rim (bright top-leading edge fading out) gives the material its thickness.
 *   - Shadows are soft and neutral — no colored glows.
 *   - Controls respond to touch by gently compressing (spring), like iOS glass controls.
 *   - Color comes only from the single tint (AccentTint) and semantic system colors.
 */

/** Specular rim brush shared by every glass surface. */
fun glassRimBrush(isDarkTheme: Boolean): Brush = Brush.linearGradient(
    colors = if (isDarkTheme) {
        listOf(GlassEdgeDarkStart, GlassEdgeDarkMid, GlassEdgeDarkEnd)
    } else {
        listOf(GlassEdgeHighlightStart, GlassEdgeHighlightMid, GlassEdgeHighlightEnd)
    },
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

/** Spring press feedback used by glass controls. */
@Composable
private fun Modifier.glassPress(interactionSource: MutableInteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "glassPress"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

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

/**
 * Liquid Glass Card — content container.
 * Thick, mostly opaque material so text stays legible, with a specular rim and soft shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 6.dp,
    shadowColor: Color = GlassShadow,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCardInternal(
        modifier = modifier,
        isDarkTheme = isDarkTheme,
        onClick = onClick,
        cornerRadius = cornerRadius,
        elevation = elevation,
        shadowColor = shadowColor,
        backgroundColor = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight,
        content = content
    )
}

@Composable
private fun GlassCardInternal(
    modifier: Modifier,
    isDarkTheme: Boolean,
    onClick: (() -> Unit)?,
    cornerRadius: Dp,
    elevation: Dp,
    shadowColor: Color,
    backgroundColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.glassPress(interactionSource) else Modifier)
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = shadowColor,
                ambientColor = shadowColor
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = backgroundColor,
        contentColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight,
        border = BorderStroke(width = 0.5.dp, brush = glassRimBrush(isDarkTheme))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Elevated Glass Card — hero or featured content, one level above [GlassCard].
 */
@Composable
fun GlassCardElevated(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCardInternal(
        modifier = modifier,
        isDarkTheme = isDarkTheme,
        onClick = onClick,
        cornerRadius = cornerRadius,
        elevation = 12.dp,
        shadowColor = GlassShadowElevated,
        backgroundColor = if (isDarkTheme) GlassSurfaceElevatedDark else GlassSurfaceElevatedLight,
        content = content
    )
}

/**
 * Liquid Glass Button — secondary action. Thin translucent glass capsule.
 */
@Composable
fun GlassButton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(50.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .glassPress(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = shape,
        color = if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim,
        contentColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight,
        border = BorderStroke(0.5.dp, glassRimBrush(isDarkTheme))
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Prominent Glass Button — primary CTA. Tinted glass: solid [AccentTint] with a specular sheen
 * on its upper half, following the iOS "prominent glass" button style.
 */
@Composable
fun GlassButtonAccent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(50.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .glassPress(interactionSource)
            .shadow(6.dp, shape, spotColor = GlassShadow, ambientColor = GlassShadow)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = shape,
        color = AccentTint,
        contentColor = OnAccent,
        border = BorderStroke(
            0.5.dp,
            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0f)))
        )
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0f))),
                shape = shape
            )
        ) {
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = 44.dp)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

/**
 * Glass Stat Chip — small capsule of glass for a stat.
 */
@Composable
fun GlassStatChip(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight,
        contentColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight,
        border = BorderStroke(0.5.dp, glassRimBrush(isDarkTheme))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            label()
        }
    }
}

/**
 * Section header — plain bold title, HIG style (no decorative bars).
 */
@Composable
fun GlassSectionHeader(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    title: String
) {
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}
