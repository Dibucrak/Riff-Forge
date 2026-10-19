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
 * Every token is adaptive: it resolves from [LocalRiffDarkTheme], provided by [RiffForgeTheme].
 */
val LocalRiffDarkTheme = staticCompositionLocalOf { true }

private inline val isDark: Boolean
    @Composable @ReadOnlyComposable get() = LocalRiffDarkTheme.current

// ── Raw system values (light / dark) ──────────────────────────────────────

// Tint — systemOrange. Light uses Apple's increased-contrast variant so text passes 4.5:1 on white.
internal val ForgeOrangeLight = Color(0xFFC93400)
internal val ForgeOrangeDark = Color(0xFFFF9F0A)

internal val SystemGreenLight = Color(0xFF248A3D)
internal val SystemGreenDark = Color(0xFF30D158)
internal val SystemRedLight = Color(0xFFD70015)
internal val SystemRedDark = Color(0xFFFF453A)
internal val SystemYellowLight = Color(0xFFA05A00)
internal val SystemYellowDark = Color(0xFFFFD60A)

// System backgrounds
internal val SystemBackgroundDark = Color(0xFF000000)
internal val SecondarySystemBackgroundDark = Color(0xFF1C1C1E)
internal val TertiarySystemBackgroundDark = Color(0xFF2C2C2E)
internal val SystemGroupedBackgroundLight = Color(0xFFF2F2F7)
internal val SecondarySystemGroupedBackgroundLight = Color(0xFFFFFFFF)

// Labels
internal val LabelLight = Color(0xFF000000)
internal val LabelDark = Color(0xFFFFFFFF)

// ── Material 3 Compatibility (kept for Scaffold / text fields) ────────────

val PrimaryLight = ForgeOrangeLight
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFFE3CC)
val OnPrimaryContainerLight = Color(0xFF3D1300)

val SecondaryLight = Color(0xFF3C3C43)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE5E5EA)
val OnSecondaryContainerLight = Color(0xFF1C1C1E)

val BackgroundLight = SystemGroupedBackgroundLight
val OnBackgroundLight = LabelLight
val SurfaceLight = SecondarySystemGroupedBackgroundLight
val OnSurfaceLight = LabelLight

val PrimaryDark = ForgeOrangeDark
val OnPrimaryDark = Color(0xFF000000)
val PrimaryContainerDark = Color(0xFF4A2A00)
val OnPrimaryContainerDark = Color(0xFFFFDDB3)

val SecondaryDark = Color(0xFFEBEBF5)
val OnSecondaryDark = Color(0xFF1C1C1E)
val SecondaryContainerDark = TertiarySystemBackgroundDark
val OnSecondaryContainerDark = LabelDark

val BackgroundDark = SystemBackgroundDark
val OnBackgroundDark = LabelDark
val SurfaceDark = SecondarySystemBackgroundDark
val OnSurfaceDark = LabelDark

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

// ── Backgrounds ───────────────────────────────────────────────────────────

/** Base canvas behind every glass panel. */
val GlassBackgroundBase: Color
    @Composable @ReadOnlyComposable get() = if (isDark) SystemBackgroundDark else SystemGroupedBackgroundLight

/** Very soft ambient light behind the glass so the material has something to refract. */
val GlassAmbientLight: Color
    @Composable @ReadOnlyComposable get() =
        if (isDark) ForgeOrangeDark.copy(alpha = 0.10f) else ForgeOrangeDark.copy(alpha = 0.08f)

// ── Liquid Glass materials (adaptive, neutral) ────────────────────────────

/** Content glass (cards) — thick material, close to secondarySystemBackground. */
val GlassSurface: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xD91C1C1E) else Color(0xE6FFFFFF)

/** Elevated glass (hero cards, sheets) — tertiary level. */
val GlassSurfaceElevated: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xE62C2C2E) else Color(0xF7FFFFFF)

/** Thin glass for floating controls (tab bar, glass buttons). */
val GlassSurfaceThin: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xB81C1C1E) else Color(0xC7FFFFFF)

/** Fill for unselected chips / segments — systemFill (tertiary). */
val GlassSurfaceDim: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x3D767680) else Color(0x1F767680)

/** Hairline separator. */
val GlassSeparator: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x2EFFFFFF) else Color(0x1F3C3C43)

// Specular rim — light hits the top-leading edge and fades out.
val GlassEdgeHighlightStart: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x3DFFFFFF) else Color(0xFFFFFFFF)
val GlassEdgeHighlightMid: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x14FFFFFF) else Color(0x0A000000)
val GlassEdgeHighlightEnd: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x0AFFFFFF) else Color(0x14000000)

// Neutral soft shadows (Apple never tints shadows)
val GlassShadow: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x99000000) else Color(0x24000000)
val GlassShadowElevated: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xB3000000) else Color(0x2E000000)

// ── Labels (adaptive, HIG label / secondaryLabel / tertiaryLabel) ─────────

val TextOnGlassPrimary: Color
    @Composable @ReadOnlyComposable get() = if (isDark) LabelDark else LabelLight
val TextOnGlassSecondary: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x99EBEBF5) else Color(0x993C3C43)
val TextOnGlassDim: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0x70EBEBF5) else Color(0x7A3C3C43)
