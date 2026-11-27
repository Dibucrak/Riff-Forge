package com.riffforge.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.riffforge.core.domain.util.ThemeMode

/**
 * RiffForge palette — Authentic Sequoia themes + System (ForgeOrange).
 *
 * Sequoia Themes:
 * - Moonlight (Dark & Light): Vibrant violet accent, sky blue keywords, pink function, warm peach variable.
 * - Monochrome (Dark & Light): Refined slate accents, cool grays, elegant minimalist feel.
 * - Retro (Dark & Light): Vintage steel/petrol blue accents, terracotta strings, sage green keywords, warm amber.
 * - System: Forge Orange accent with Apple HIG system surfaces and high contrast.
 */
val LocalRiffTheme = staticCompositionLocalOf { ThemeMode.SYSTEM }

// Helper to determine if a color has high luminance
internal fun isColorLight(c: Color): Boolean {
    val luminance = 0.299 * c.red + 0.587 * c.green + 0.114 * c.blue
    return luminance > 0.5f
}

// ── ThemeColors Definition ──────────────────────────────────────────────────

data class ThemeColors(
    val bg: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val fg: Color,
    val textPrimary: Color,
    val caret: Color,
    val keyword: Color,
    val string: Color,
    val function: Color,
    val variable: Color,
    val comment: Color,
    val outline: Color,
    val systemGreen: Color,
    val systemRed: Color,
    val systemYellow: Color,
    val isDark: Boolean
)

// ── Sequoia raw palette values ──────────────────────────────────────────────

// Moonlight Dark
private val M_D = ThemeColors(
    bg = Color(0xFF0F1014),
    surface = Color(0xFF111216),
    surfaceElevated = Color(0xFF18191F),
    surfaceVariant = Color(0xFF1B1C22),
    fg = Color(0xFF868690),
    textPrimary = Color(0xFFFDFDFE),
    caret = Color(0xFFC58FFF),
    keyword = Color(0xFF8EB6F5),
    string = Color(0xFF9898A6),
    function = Color(0xFFF58EE0),
    variable = Color(0xFFFFBB88),
    comment = Color(0xFF43444D),
    outline = Color(0x2EFFFFFF),
    systemGreen = Color(0xFF30D158),
    systemRed = Color(0xFFFF453A),
    systemYellow = Color(0xFFFFD60A),
    isDark = true
)

// Moonlight Light
private val M_L = ThemeColors(
    bg = Color(0xFFEDEEF2),
    surface = Color(0xFFE2E3E8),
    surfaceElevated = Color(0xFFD8D9DF),
    surfaceVariant = Color(0xFFD3D4DB),
    fg = Color(0xFF282930),
    textPrimary = Color(0xFF121316),
    caret = Color(0xFF9A5FD9),
    keyword = Color(0xFF4A85D4),
    string = Color(0xFF6A6A78),
    function = Color(0xFFC94DA8),
    variable = Color(0xFFD9884A),
    comment = Color(0xFF565760),
    outline = Color(0x1F282930),
    systemGreen = Color(0xFF248A3D),
    systemRed = Color(0xFFD70015),
    systemYellow = Color(0xFFA05A00),
    isDark = false
)

// Monochrome Dark
private val MC_D = ThemeColors(
    bg = Color(0xFF0F1014),
    surface = Color(0xFF111216),
    surfaceElevated = Color(0xFF18191F),
    surfaceVariant = Color(0xFF1B1C22),
    fg = Color(0xFF868690),
    textPrimary = Color(0xFFFFFFFF),
    caret = Color(0xFF7C829D),
    keyword = Color(0xFF626983),
    string = Color(0xFFD3D5DE),
    function = Color(0xFF999EB2),
    variable = Color(0xFFB6BAC8),
    comment = Color(0xFF43444D),
    outline = Color(0x2EFFFFFF),
    systemGreen = Color(0xFF30D158),
    systemRed = Color(0xFFFF453A),
    systemYellow = Color(0xFFFFD60A),
    isDark = true
)

// Monochrome Light
private val MC_L = ThemeColors(
    bg = Color(0xFFEDEEF2),
    surface = Color(0xFFE2E3E8),
    surfaceElevated = Color(0xFFD8D9DF),
    surfaceVariant = Color(0xFFD3D4DB),
    fg = Color(0xFF282930),
    textPrimary = Color(0xFF101114),
    caret = Color(0xFF5F6370),
    keyword = Color(0xFF2E3038),
    string = Color(0xFF50535E),
    function = Color(0xFF525666),
    variable = Color(0xFF454752),
    comment = Color(0xFF9DA2AD),
    outline = Color(0x1F282930),
    systemGreen = Color(0xFF248A3D),
    systemRed = Color(0xFFD70015),
    systemYellow = Color(0xFFA05A00),
    isDark = false
)

// Retro Dark
private val R_D = ThemeColors(
    bg = Color(0xFF0F1014),
    surface = Color(0xFF111216),
    surfaceElevated = Color(0xFF18191F),
    surfaceVariant = Color(0xFF1B1C22),
    fg = Color(0xFF868690),
    textPrimary = Color(0xFFF8F6F2),
    caret = Color(0xFF5C87A4),
    keyword = Color(0xFF648F68),
    string = Color(0xFFDA674B),
    function = Color(0xFF829FA7),
    variable = Color(0xFFA27E57),
    comment = Color(0xFF43444D),
    outline = Color(0x2EFFFFFF),
    systemGreen = Color(0xFF648F68),
    systemRed = Color(0xFFDA674B),
    systemYellow = Color(0xFFD4A359),
    isDark = true
)

// Retro Light
private val R_L = ThemeColors(
    bg = Color(0xFFEDEEF2),
    surface = Color(0xFFE2E3E8),
    surfaceElevated = Color(0xFFD8D9DF),
    surfaceVariant = Color(0xFFD3D4DB),
    fg = Color(0xFF282930),
    textPrimary = Color(0xFF1F1E1B),
    caret = Color(0xFF456A82),
    keyword = Color(0xFF4A704E),
    string = Color(0xFFBF5238),
    function = Color(0xFF5F7982),
    variable = Color(0xFF8A6539),
    comment = Color(0xFF565760),
    outline = Color(0x1F282930),
    systemGreen = Color(0xFF4A704E),
    systemRed = Color(0xFFBF5238),
    systemYellow = Color(0xFFA05A00),
    isDark = false
)

// System Dark (Warm Studio Dark — Stitch Guide)
private val S_D = ThemeColors(
    bg = Color(0xFF141211),
    surface = Color(0xFF1D1816),
    surfaceElevated = Color(0xFF2A221E),
    surfaceVariant = Color(0xFF231D1A),
    fg = Color(0xFFB5A8A1),
    textPrimary = Color(0xFFF7F4F2),
    caret = Color(0xFFFF8860),
    keyword = Color(0xFF30D158),
    string = Color(0xFFE0D5CE),
    function = Color(0xFF342A25),
    variable = Color(0xFF231D1A),
    comment = Color(0xFF6B5F59),
    outline = Color(0x33D6BEB2),
    systemGreen = Color(0xFF34C759),
    systemRed = Color(0xFFFF5A4E),
    systemYellow = Color(0xFFFFD60A),
    isDark = true
)

// System Light (Warm Forge Light — Stitch Guide)
private val S_L = ThemeColors(
    bg = Color(0xFFF6F6F8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF5EAE3),
    surfaceVariant = Color(0xFFFCF7F4),
    fg = Color(0xFF5D4D45),
    textPrimary = Color(0xFF1A1412),
    caret = Color(0xFFC83700),
    keyword = Color(0xFF248A3D),
    string = Color(0xFF5D4D45),
    function = Color(0xFFEFE4DD),
    variable = Color(0xFF231D1A),
    comment = Color(0xFF9E8D84),
    outline = Color(0x2EA88A7C),
    systemGreen = Color(0xFF28B44D),
    systemRed = Color(0xFFD70015),
    systemYellow = Color(0xFFA05A00),
    isDark = false
)

// ── Resolver Function ───────────────────────────────────────────────────────

fun resolveThemeColors(mode: ThemeMode, isSysDark: Boolean): ThemeColors {
    return when (mode) {
        ThemeMode.MOONLIGHT_DARK -> M_D
        ThemeMode.MOONLIGHT_LIGHT -> M_L
        ThemeMode.MONOCHROME_DARK -> MC_D
        ThemeMode.MONOCHROME_LIGHT -> MC_L
        ThemeMode.RETRO_DARK -> R_D
        ThemeMode.RETRO_LIGHT -> R_L
        ThemeMode.SYSTEM -> if (isSysDark) S_D else S_L
    }
}

fun isThemeDark(mode: ThemeMode, isSysDark: Boolean): Boolean {
    return when (mode) {
        ThemeMode.MOONLIGHT_DARK, ThemeMode.MONOCHROME_DARK, ThemeMode.RETRO_DARK -> true
        ThemeMode.MOONLIGHT_LIGHT, ThemeMode.MONOCHROME_LIGHT, ThemeMode.RETRO_LIGHT -> false
        ThemeMode.SYSTEM -> isSysDark
    }
}

@Composable
@ReadOnlyComposable
fun isThemeDark(): Boolean {
    return isThemeDark(LocalRiffTheme.current, isSystemInDarkTheme())
}

val CurrentThemeColors: ThemeColors
    @Composable
    @ReadOnlyComposable
    get() = resolveThemeColors(LocalRiffTheme.current, isSystemInDarkTheme())

private val tc: ThemeColors
    @Composable
    @ReadOnlyComposable
    get() = CurrentThemeColors

// ── Semantic Tokens ─────────────────────────────────────────────────────────

val AccentTint: Color
    @Composable @ReadOnlyComposable get() = tc.caret

val OnAccent: Color
    @Composable @ReadOnlyComposable get() = if (isColorLight(tc.caret)) Color.Black else Color.White

val SystemGreen: Color
    @Composable @ReadOnlyComposable get() = tc.systemGreen

val SystemRed: Color
    @Composable @ReadOnlyComposable get() = tc.systemRed

val SystemYellow: Color
    @Composable @ReadOnlyComposable get() = tc.systemYellow

// ── Backgrounds & Glass Materials ───────────────────────────────────────────

val GlassBackgroundBase: Color
    @Composable @ReadOnlyComposable get() = tc.bg

val GlassAmbientLight: Color
    @Composable @ReadOnlyComposable get() = tc.caret.copy(alpha = if (tc.isDark) 0.10f else 0.08f)

val GlassSurface: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) tc.surface.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)

val GlassSurfaceElevated: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) tc.surfaceElevated.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.95f)

val GlassSurfaceThin: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) tc.surface.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.75f)

val GlassSurfaceDim: Color
    @Composable @ReadOnlyComposable get() = tc.variable.copy(alpha = 0.18f)

val GlassSeparator: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)

val GlassEdgeHighlightStart: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) Color.White.copy(alpha = 0.20f) else Color.White

val GlassEdgeHighlightMid: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f)

val GlassEdgeHighlightEnd: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.06f)

val GlassShadow: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) Color(0x99000000) else Color(0x24000000)

val GlassShadowElevated: Color
    @Composable @ReadOnlyComposable get() = if (tc.isDark) Color(0xB3000000) else Color(0x2E000000)

// ── Text on Glass ───────────────────────────────────────────────────────────

val TextOnGlassPrimary: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary

val TextOnGlassSecondary: Color
    @Composable @ReadOnlyComposable get() = tc.fg

val TextOnGlassDim: Color
    @Composable @ReadOnlyComposable get() = tc.comment

// ── Material 3 Compatibility Tokens ─────────────────────────────────────────

val PrimaryLight: Color
    @Composable @ReadOnlyComposable get() = tc.caret
val OnPrimaryLight: Color
    @Composable @ReadOnlyComposable get() = OnAccent
val PrimaryContainerLight: Color
    @Composable @ReadOnlyComposable get() = tc.caret.copy(alpha = 0.15f)
val OnPrimaryContainerLight: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary

val SecondaryLight: Color
    @Composable @ReadOnlyComposable get() = tc.keyword
val OnSecondaryLight: Color
    @Composable @ReadOnlyComposable get() = if (isColorLight(tc.keyword)) Color.Black else Color.White
val SecondaryContainerLight: Color
    @Composable @ReadOnlyComposable get() = tc.keyword.copy(alpha = 0.15f)
val OnSecondaryContainerLight: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary

val BackgroundLight: Color
    @Composable @ReadOnlyComposable get() = tc.bg
val OnBackgroundLight: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary
val SurfaceLight: Color
    @Composable @ReadOnlyComposable get() = tc.surface
val OnSurfaceLight: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary

val PrimaryDark: Color
    @Composable @ReadOnlyComposable get() = tc.caret
val OnPrimaryDark: Color
    @Composable @ReadOnlyComposable get() = OnAccent
val PrimaryContainerDark: Color
    @Composable @ReadOnlyComposable get() = tc.caret.copy(alpha = 0.22f)
val OnPrimaryContainerDark: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary

val SecondaryDark: Color
    @Composable @ReadOnlyComposable get() = tc.keyword
val OnSecondaryDark: Color
    @Composable @ReadOnlyComposable get() = if (isColorLight(tc.keyword)) Color.Black else Color.White
val SecondaryContainerDark: Color
    @Composable @ReadOnlyComposable get() = tc.keyword.copy(alpha = 0.22f)
val OnSecondaryContainerDark: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary

val BackgroundDark: Color
    @Composable @ReadOnlyComposable get() = tc.bg
val OnBackgroundDark: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary
val SurfaceDark: Color
    @Composable @ReadOnlyComposable get() = tc.surface
val OnSurfaceDark: Color
    @Composable @ReadOnlyComposable get() = tc.textPrimary

// ── Legacy Aliases ──────────────────────────────────────────────────────────

val ForgeOrangeLight = Color(0xFFC93400)
val ForgeOrangeDark = Color(0xFFFF9F0A)
val SystemGreenLight = Color(0xFF248A3D)
val SystemGreenDark = Color(0xFF30D158)
val SystemRedLight = Color(0xFFD70015)
val SystemRedDark = Color(0xFFFF453A)
val SystemYellowLight = Color(0xFFA05A00)
val SystemYellowDark = Color(0xFFFFD60A)

val SystemBackgroundDark: Color @Composable @ReadOnlyComposable get() = tc.bg
val SecondarySystemBackgroundDark: Color @Composable @ReadOnlyComposable get() = tc.surfaceVariant
val TertiarySystemBackgroundDark: Color @Composable @ReadOnlyComposable get() = tc.surfaceElevated
val SystemGroupedBackgroundLight: Color @Composable @ReadOnlyComposable get() = tc.bg
val SecondarySystemGroupedBackgroundLight: Color @Composable @ReadOnlyComposable get() = tc.surfaceVariant

val GlassBackgroundBaseDark: Color @Composable @ReadOnlyComposable get() = tc.bg
val GlassBackgroundBaseLight: Color @Composable @ReadOnlyComposable get() = tc.bg
val GlassAmbientLightDark: Color @Composable @ReadOnlyComposable get() = tc.caret.copy(alpha = 0.10f)
val GlassAmbientLightLight: Color @Composable @ReadOnlyComposable get() = tc.caret.copy(alpha = 0.08f)

val GlassSurfaceLightDim: Color @Composable @ReadOnlyComposable get() = tc.variable.copy(alpha = 0.18f)
val GlassSurfaceDarkDim: Color @Composable @ReadOnlyComposable get() = tc.variable.copy(alpha = 0.18f)
val GlassSurfaceElevatedLight: Color @Composable @ReadOnlyComposable get() = GlassSurfaceElevated
val GlassSurfaceElevatedDark: Color @Composable @ReadOnlyComposable get() = GlassSurfaceElevated
val GlassSurfaceThinLight: Color @Composable @ReadOnlyComposable get() = GlassSurfaceThin
val GlassSurfaceThinDark: Color @Composable @ReadOnlyComposable get() = GlassSurfaceThin

val GlassSeparatorLight: Color @Composable @ReadOnlyComposable get() = GlassSeparator
val GlassSeparatorDark: Color @Composable @ReadOnlyComposable get() = GlassSeparator

val GlassEdgeDarkStart: Color @Composable @ReadOnlyComposable get() = GlassEdgeHighlightStart
val GlassEdgeDarkMid: Color @Composable @ReadOnlyComposable get() = GlassEdgeHighlightMid
val GlassEdgeDarkEnd: Color @Composable @ReadOnlyComposable get() = GlassEdgeHighlightEnd

val TextOnGlassPrimaryLight: Color @Composable @ReadOnlyComposable get() = tc.textPrimary
val TextOnGlassSecondaryLight: Color @Composable @ReadOnlyComposable get() = tc.fg
val TextOnGlassDimLight: Color @Composable @ReadOnlyComposable get() = tc.comment
val TextOnGlassPrimaryDark: Color @Composable @ReadOnlyComposable get() = tc.textPrimary
val TextOnGlassSecondaryDark: Color @Composable @ReadOnlyComposable get() = tc.fg
val TextOnGlassDimDark: Color @Composable @ReadOnlyComposable get() = tc.comment
