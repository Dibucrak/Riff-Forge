package com.riffforge.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.ThemeMode

/**
 * Resolves the effective [ThemeMode] from [AppSettings.themeMode].
 * For Sequoia themes the mode is explicit; for SYSTEM it follows the OS preference.
 */
@Composable
fun resolveThemeMode(): ThemeMode {
    return AppSettings.themeMode
}

@Composable
fun RiffForgeTheme(
    content: @Composable () -> Unit
) {
    val themeMode = resolveThemeMode()
    val sysDark = isSystemInDarkTheme()
    val dark = isThemeDark(themeMode, sysDark)

    CompositionLocalProvider(LocalRiffTheme provides themeMode) {
        val tc = resolveThemeColors(themeMode, sysDark)

        val colorScheme = if (dark) {
            darkColorScheme(
                primary = tc.caret,
                onPrimary = if (isColorLight(tc.caret)) Color.Black else Color.White,
                primaryContainer = tc.caret.copy(alpha = 0.22f),
                onPrimaryContainer = tc.textPrimary,
                secondary = tc.keyword,
                onSecondary = if (isColorLight(tc.keyword)) Color.Black else Color.White,
                secondaryContainer = tc.keyword.copy(alpha = 0.22f),
                onSecondaryContainer = tc.textPrimary,
                tertiary = tc.function,
                onTertiary = if (isColorLight(tc.function)) Color.Black else Color.White,
                background = tc.bg,
                onBackground = tc.textPrimary,
                surface = tc.surface,
                onSurface = tc.textPrimary,
                surfaceVariant = tc.surfaceVariant,
                onSurfaceVariant = tc.fg,
                outline = tc.outline,
                error = tc.systemRed,
                onError = Color.White
            )
        } else {
            lightColorScheme(
                primary = tc.caret,
                onPrimary = if (isColorLight(tc.caret)) Color.Black else Color.White,
                primaryContainer = tc.caret.copy(alpha = 0.15f),
                onPrimaryContainer = tc.textPrimary,
                secondary = tc.keyword,
                onSecondary = if (isColorLight(tc.keyword)) Color.Black else Color.White,
                secondaryContainer = tc.keyword.copy(alpha = 0.15f),
                onSecondaryContainer = tc.textPrimary,
                tertiary = tc.function,
                onTertiary = if (isColorLight(tc.function)) Color.Black else Color.White,
                background = tc.bg,
                onBackground = tc.textPrimary,
                surface = tc.surface,
                onSurface = tc.textPrimary,
                surfaceVariant = tc.surfaceVariant,
                onSurfaceVariant = tc.fg,
                outline = tc.outline,
                error = tc.systemRed,
                onError = Color.White
            )
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
