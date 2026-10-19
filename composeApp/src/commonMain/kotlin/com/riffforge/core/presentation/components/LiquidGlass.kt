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

/**
 * Liquid Glass components — following Apple's Human Interface Guidelines for materials.
 *
 * Principles applied:
 *   - Glass is a neutral, translucent material: it never carries brand color itself.
 *   - A specular rim (bright top-leading edge fading out) gives the material its thickness.
 *   - Shadows are soft and neutral — no colored glows.
 *   - Controls respond to touch by gently compressing (spring), like iOS glass controls.
 *   - Color comes only from the single tint ([AccentTint]) and semantic system colors.
 *   - Every token adapts to light / dark through [LocalRiffDarkTheme].
 */

/** Specular rim brush shared by every glass surface. */
@Composable
fun glassRimBrush(): Brush = Brush.linearGradient(
    colors = listOf(
        GlassEdgeHighlightStart,
        GlassEdgeHighlightMid,
        GlassEdgeHighlightEnd
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

/** Spring press feedback used by glass controls. */
@Composable
private fun Modifier.glassPress(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Liquid Glass Card — content container.
 *
 * Thick, mostly opaque material (close to secondarySystemBackground) so text stays legible,
 * with a specular rim and a soft neutral shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 6.dp,
    shadowColor: Color = GlassShadow,
    backgroundColor: Color = GlassSurface,
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
        contentColor = TextOnGlassPrimary,
        border = BorderStroke(width = 0.5.dp, brush = glassRimBrush())
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
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        cornerRadius = cornerRadius,
        elevation = 12.dp,
        shadowColor = GlassShadowElevated,
        backgroundColor = GlassSurfaceElevated,
        content = content
    )
}

/**
 * Liquid Glass Button — secondary action. Thin translucent glass capsule.
 */
@Composable
fun GlassButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cornerRadius: Dp = 50.dp,
    backgroundColor: Color = GlassSurfaceDim,
    contentColor: Color = TextOnGlassPrimary,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .glassPress(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(width = 0.5.dp, brush = glassRimBrush())
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Prominent Glass Button — primary CTA. Tinted glass: solid [AccentTint] with a specular
 * sheen on its upper half, following the iOS "prominent glass" button style.
 */
@Composable
fun GlassButtonAccent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cornerRadius: Dp = 50.dp,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val tint = AccentTint

    Surface(
        modifier = modifier
            .glassPress(interactionSource)
            .shadow(
                elevation = 6.dp,
                shape = shape,
                spotColor = GlassShadow,
                ambientColor = GlassShadow
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = shape,
        color = tint,
        contentColor = OnAccent,
        border = BorderStroke(
            width = 0.5.dp,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.0f))
            )
        )
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.0f))
                ),
                shape = shape
            )
        ) {
            CompositionLocalProvider(LocalContentColor provides OnAccent) {
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
}

/**
 * Liquid Glass Stat Chip — a single metric on glass. Color lives in the value, not the surface.
 */
@Composable
fun GlassStatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accentColor: Color = AccentTint,
    cornerRadius: Dp = 16.dp
) {
    val shape = RoundedCornerShape(cornerRadius)

    Surface(
        modifier = modifier,
        shape = shape,
        color = GlassSurface,
        border = BorderStroke(width = 0.5.dp, brush = glassRimBrush())
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnGlassSecondary
            )
        }
    }
}

/**
 * Section header — plain bold title, HIG style (no decorative bars).
 */
@Composable
fun GlassSectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextOnGlassPrimary
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnGlassSecondary
            )
        }
    }
}

/**
 * Liquid Glass Background — neutral system canvas (black / #F2F2F7) with a single, very soft
 * warm ambient light near the top so the translucent glass has something to refract.
 */
@Composable
fun GlassBackground(
    content: @Composable () -> Unit
) {
    val ambient = GlassAmbientLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassBackgroundBase)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-160).dp)
                .size(420.dp)
                .background(
                    brush = Brush.radialGradient(colors = listOf(ambient, Color.Transparent)),
                    shape = CircleShape
                )
        )

        CompositionLocalProvider(LocalContentColor provides TextOnGlassPrimary) {
            content()
        }
    }
}
