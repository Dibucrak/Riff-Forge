package com.riffforge.core.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.ThemeMode

@Composable
fun RiffForgeTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeMode = AppSettings.themeMode
    val sysDark = isSystemInDarkTheme()
    val dark = isThemeDark(themeMode, sysDark)

    CompositionLocalProvider(LocalRiffTheme provides themeMode) {
        val tc = resolveThemeColors(themeMode, sysDark)

        val colorScheme = when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeMode == ThemeMode.SYSTEM -> {
                val context = LocalContext.current
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            dark -> darkColorScheme(
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
            else -> lightColorScheme(
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

        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}