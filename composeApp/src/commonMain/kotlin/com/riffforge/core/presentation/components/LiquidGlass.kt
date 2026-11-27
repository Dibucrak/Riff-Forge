package com.riffforge.core.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riffforge.core.presentation.theme.*

/**
 * Liquid Glass Component System — strictly adhering to Apple Human Interface Guidelines (HIG).
 *
 * Principles:
 *   - Translucent materials (frosted glass) that defer to content.
 *   - Specular rims (0.5dp hairline) simulating light reflections on glass edges.
 *   - Neutral, soft volumetric shadows — no artificial colored neon glows.
 *   - Continuous squircles (RoundedCornerShape 14–20dp) matching iOS 17/18 geometry.
 *   - Natural tactile feedback: spring scale (0.97f) + subtle dimming on touch.
 *   - Apple button hierarchy: Prominent (Filled), Tinted (Soft), Glass (Gray/Neutral).
 *   - Inset Grouped lists & navigation bars adhering to iOS architecture.
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

/** Spring press feedback with subtle scale and alpha dimming used by glass controls. */
@Composable
fun Modifier.glassPress(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glassScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled && isPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "glassAlpha"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

/**
 * Liquid Glass Card — Apple Inset Card container.
 * Squircles with continuous corner curvature, specular edge highlight, and soft neutral shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 4.dp,
    shadowColor: Color = GlassShadow,
    backgroundColor: Color = GlassSurface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
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
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * Elevated Glass Card — Hero / featured content container.
 */
@Composable
fun GlassCardElevated(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 22.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        cornerRadius = cornerRadius,
        elevation = 10.dp,
        shadowColor = GlassShadowElevated,
        backgroundColor = GlassSurfaceElevated,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Inset Grouped Section Container — Apple's signature list layout.
 * Groups multiple rows with hairline dividers between them inside a single refined glass card.
 */
@Composable
fun GlassInsetGroup(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = cornerRadius,
        contentPadding = PaddingValues(0.dp),
        content = content
    )
}

/**
 * Hairline separator for inset lists and toolbars.
 */
@Composable
fun GlassSeparator(
    modifier: Modifier = Modifier,
    startIndent: Dp = 0.dp
) {
    HorizontalDivider(
        modifier = modifier.padding(start = startIndent),
        thickness = 0.5.dp,
        color = GlassSeparator
    )
}

/**
 * Primary Prominent Glass Button (Apple Filled Button Style).
 * Uses rich solid [AccentTint] with a subtle specular sheen, continuous rounded rectangle (14-16dp),
 * and tactile touch compression.
 */
@Composable
fun GlassButtonAccent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cornerRadius: Dp = 16.dp,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (enabled) AccentTint else GlassSurfaceDim

    Surface(
        modifier = modifier
            .glassPress(interactionSource, enabled = enabled)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = shape,
                spotColor = GlassShadow,
                ambientColor = GlassShadow
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = shape,
        color = tint,
        contentColor = if (enabled) OnAccent else TextOnGlassDim,
        border = BorderStroke(
            width = 0.5.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (enabled) 0.40f else 0.10f),
                    Color.White.copy(alpha = 0.0f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                shape = shape
            )
        ) {
            CompositionLocalProvider(LocalContentColor provides (if (enabled) OnAccent else TextOnGlassDim)) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 50.dp)
                        .padding(horizontal = 22.dp, vertical = 13.dp),
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
 * Secondary Tinted Glass Button (Apple Tinted Button Style).
 * Uses translucent accent fill ([AccentTint] with 14% alpha) and vibrant accent foreground.
 */
@Composable
fun GlassButtonTinted(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cornerRadius: Dp = 14.dp,
    enabled: Boolean = true,
    tintColor: Color = AccentTint,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .glassPress(interactionSource, enabled = enabled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = shape,
        color = tintColor.copy(alpha = 0.14f),
        contentColor = tintColor,
        border = BorderStroke(
            width = 0.5.dp,
            color = tintColor.copy(alpha = 0.28f)
        )
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .padding(horizontal = 18.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Tertiary / Neutral Glass Button (Apple Gray / Glass Button Style).
 * Translucent glass capsule or squircle.
 */
@Composable
fun GlassButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cornerRadius: Dp = 14.dp,
    backgroundColor: Color = GlassSurfaceDim,
    contentColor: Color = TextOnGlassPrimary,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .glassPress(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(width = 0.5.dp, brush = glassRimBrush())
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Circular Glass Icon Button — used in navigation bars, toolbars, and close/back buttons.
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = GlassSurfaceDim,
    contentColor: Color = TextOnGlassPrimary,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .size(size)
            .glassPress(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(width = 0.5.dp, brush = glassRimBrush())
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Native Apple HIG Navigation Bar Header for sub-screens.
 * Clean, translucent, safe-area aware — eliminates the artificial card-wrapped headers.
 */
@Composable
fun GlassNavigationHeader(
    title: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            GlassIconButton(
                onClick = onNavigateUp,
                size = 38.dp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    tint = TextOnGlassPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextOnGlassPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnGlassSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (trailingContent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                trailingContent()
            }
        }
    }
}

/**
 * Apple UISegmentedControl style picker.
 * Recessed glass pill track with sliding/active segment thumb.
 */
@Composable
fun <T> GlassSegmentedControl(
    items: List<T>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: (T) -> String = { it.toString() }
) {
    val trackShape = RoundedCornerShape(12.dp)
    val thumbShape = RoundedCornerShape(10.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = trackShape,
        color = GlassSurfaceDim,
        border = BorderStroke(width = 0.5.dp, brush = glassRimBrush())
    ) {
        Row(
            modifier = Modifier
                .padding(3.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(thumbShape)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(elevation = 3.dp, shape = thumbShape, spotColor = GlassShadow)
                                    .background(GlassSurfaceElevated)
                                    .border(0.5.dp, glassRimBrush(), thumbShape)
                            } else {
                                Modifier.background(Color.Transparent)
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelectIndex(index) }
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemLabel(item),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) AccentTint else TextOnGlassSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Metric Chip — Single metric on glass following Apple HIG.
 */
@Composable
fun GlassStatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accentColor: Color = AccentTint,
    cornerRadius: Dp = 16.dp,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.glassPress(interactionSource) else Modifier)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        color = GlassSurface,
        border = BorderStroke(width = 0.5.dp, brush = glassRimBrush())
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnGlassSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Compact Pill Badge for metadata (Key, Tempo, Status, Difficulty).
 */
@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    tintColor: Color = AccentTint,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = tintColor.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, tintColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tintColor
            )
        }
    }
}

/**
 * Section Header — Apple Inset Grouped style header with optional action button.
 */
@Composable
fun GlassSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextOnGlassPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnGlassSecondary
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AccentTint,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onActionClick
                    )
                    .padding(start = 8.dp)
            )
        }
    }
}

/**
 * Liquid Glass Background — neutral system canvas with soft refractive ambient light.
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
        // Ambient soft light orb for glass refraction
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-120).dp)
                .size(380.dp)
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
