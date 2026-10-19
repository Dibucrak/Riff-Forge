package com.riffforge.core.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * RiffForge palette — based on Apple Human Interface Guidelines.
 *
 * - Neutral backgrounds (true black in dark mode, grouped gray #F2F2F7 in light mode).
 * - A single tint color ("forge orange", systemOrange) drives interactive elements.
 * - Semantic system colors (green / red / yellow) only carry meaning: success, error, warning.
 * - Liquid Glass surfaces are translucent neutral materials with a specular rim, never colored.
 *
 * Surface/text tokens come in explicit Light/Dark pairs (screens pick them with `isDarkTheme`).
 * Accent tokens are adaptive and resolve from [LocalRiffDarkTheme], provided by [RiffForgeTheme].
 */
val LocalRiffDarkTheme = staticCompositionLocalOf { true }

private inline val isDark: Boolean
    @Composable @ReadOnlyComposable get() = LocalRiffDarkTheme.current

// ── Raw system values (light / dark) ──────────────────────────────────────

// Tint — systemOrange. Light uses Apple's increased-contrast variant so text passes 4.5:1 on white.
val ForgeOrangeLight = Color(0xFFC93400)
val ForgeOrangeDark = Color(0xFFFF9F0A)

val SystemGreenLight = Color(0xFF248A3D)
val SystemGreenDark = Color(0xFF30D158)
val SystemRedLight = Color(0xFFD70015)
val SystemRedDark = Color(0xFFFF453A)
val SystemYellowLight = Color(0xFFA05A00)
val SystemYellowDark = Color(0xFFFFD60A)

// System backgrounds
val SystemBackgroundDark = Color(0xFF000000)
val SecondarySystemBackgroundDark = Color(0xFF1C1C1E)
val TertiarySystemBackgroundDark = Color(0xFF2C2C2E)
val SystemGroupedBackgroundLight = Color(0xFFF2F2F7)
val SecondarySystemGroupedBackgroundLight = Color(0xFFFFFFFF)

// ── Material 3 Compatibility (kept for Scaffold / text fields / dialogs) ──

//tema claro
val PrimaryLight = ForgeOrangeLight
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFFE3CC)
val OnPrimaryContainerLight = Color(0xFF3D1300)

val SecondaryLight = Color(0xFF3C3C43)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE5E5EA)
val OnSecondaryContainerLight = Color(0xFF1C1C1E)

val BackgroundLight = SystemGroupedBackgroundLight
val OnBackgroundLight = Color(0xFF000000)
val SurfaceLight = SecondarySystemGroupedBackgroundLight
val OnSurfaceLight = Color(0xFF000000)

//tema oscuro
val PrimaryDark = ForgeOrangeDark
val OnPrimaryDark = Color(0xFF000000)
val PrimaryContainerDark = Color(0xFF4A2A00)
val OnPrimaryContainerDark = Color(0xFFFFDDB3)

val SecondaryDark = Color(0xFFEBEBF5)
val OnSecondaryDark = Color(0xFF1C1C1E)
val SecondaryContainerDark = TertiarySystemBackgroundDark
val OnSecondaryContainerDark = Color(0xFFFFFFFF)

val BackgroundDark = SystemBackgroundDark
val OnBackgroundDark = Color(0xFFFFFFFF)
val SurfaceDark = SecondarySystemBackgroundDark
val OnSurfaceDark = Color(0xFFFFFFFF)

// ── Accents (adaptive) ────────────────────────────────────────────────────

/** The single interactive tint (buttons, selection, active icons, progress). */
val AccentTint: Color
    @Composable @ReadOnlyComposable get() = if (isDark) ForgeOrangeDark else ForgeOrangeLight

/** Foreground for content placed on a solid [AccentTint] fill. */
val OnAccent: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)

/** Success / in tune / correct. */
val SystemGreen: Color
    @Composable @ReadOnlyComposable get() = if (isDark) SystemGreenDark else SystemGreenLight

/** Error / destructive / out of tune. */
val SystemRed: Color
    @Composable @ReadOnlyComposable get() = if (isDark) SystemRedDark else SystemRedLight

/** Warning / highlight / streaks. */
val SystemYellow: Color
    @Composable @ReadOnlyComposable get() = if (isDark) SystemYellowDark else SystemYellowLight

// Neutral soft shadows (Apple never tints shadows)
val GlassShadow: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x99000000) else Color(0x24000000)
val GlassShadowElevated: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xB3000000) else Color(0x2E000000)

// ── Backgrounds ───────────────────────────────────────────────────────────

val GlassBackgroundBaseDark = SystemBackgroundDark
val GlassBackgroundBaseLight = SystemGroupedBackgroundLight

/** Very soft warm ambient light behind the glass so the material has something to refract. */
val GlassAmbientLightDark = ForgeOrangeDark.copy(alpha = 0.10f)
val GlassAmbientLightLight = ForgeOrangeDark.copy(alpha = 0.08f)

// ── Liquid Glass materials — light mode ───────────────────────────────────

val GlassSurfaceLight = Color(0xE6FFFFFF)          // content glass (cards)
val GlassSurfaceLightDim = Color(0x1F767680)       // systemFill — chips, unselected segments
val GlassSurfaceElevatedLight = Color(0xF7FFFFFF)  // hero cards, sheets
val GlassSurfaceThinLight = Color(0xC7FFFFFF)      // floating controls (tab bar)
val GlassSeparatorLight = Color(0x1F3C3C43)

// Specular rim — light mode (bright top-leading edge, faint definition at the far edge)
val GlassEdgeHighlightStart = Color(0xFFFFFFFF)
val GlassEdgeHighlightMid = Color(0x0A000000)
val GlassEdgeHighlightEnd = Color(0x14000000)

// ── Liquid Glass materials — dark mode ────────────────────────────────────

val GlassSurfaceDark = Color(0xD91C1C1E)           // content glass (cards)
val GlassSurfaceDarkDim = Color(0x3D767680)        // systemFill — chips, unselected segments
val GlassSurfaceElevatedDark = Color(0xE62C2C2E)   // hero cards, sheets
val GlassSurfaceThinDark = Color(0xB81C1C1E)       // floating controls (tab bar)
val GlassSeparatorDark = Color(0x2EFFFFFF)

// Specular rim — dark mode
val GlassEdgeDarkStart = Color(0x3DFFFFFF)
val GlassEdgeDarkMid = Color(0x14FFFFFF)
val GlassEdgeDarkEnd = Color(0x0AFFFFFF)

// ── Labels (HIG label / secondaryLabel / tertiaryLabel) ───────────────────

val TextOnGlassPrimaryLight = Color(0xFF000000)
val TextOnGlassSecondaryLight = Color(0x993C3C43)
val TextOnGlassDimLight = Color(0x7A3C3C43)

val TextOnGlassPrimaryDark = Color(0xFFFFFFFF)
val TextOnGlassSecondaryDark = Color(0x99EBEBF5)
val TextOnGlassDimDark = Color(0x70EBEBF5)
