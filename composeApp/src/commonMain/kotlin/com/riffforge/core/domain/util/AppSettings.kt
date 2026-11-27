package com.riffforge.core.domain.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

enum class ThemeMode {
    SYSTEM,
    MOONLIGHT_DARK, MOONLIGHT_LIGHT,
    MONOCHROME_DARK, MONOCHROME_LIGHT,
    RETRO_DARK, RETRO_LIGHT
}

/**
 * App-wide settings holder with Compose reactive state (KMP version).
 * Persists theme and notation preferences using multiplatform-settings.
 * Triggers instant recomposition when settings change.
 */
object AppSettings {

    private val settings: Settings = Settings()

    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_SPANISH_NOTATION = "spanish_notation"

    var useSpanishNotation: Boolean by mutableStateOf(
        settings.get(KEY_SPANISH_NOTATION, false)
    )
        private set

    var themeMode: ThemeMode by mutableStateOf(
        run {
            val saved = settings.get(KEY_THEME_MODE, "")
            if (saved.isNotEmpty()) {
                runCatching { ThemeMode.valueOf(saved) }.getOrDefault(ThemeMode.SYSTEM)
            } else {
                ThemeMode.SYSTEM
            }
        }
    )
        private set

    fun toggleSpanishNotation(): Boolean {
        useSpanishNotation = !useSpanishNotation
        settings.set(KEY_SPANISH_NOTATION, useSpanishNotation)
        return useSpanishNotation
    }

    fun setSpanishNotation(useSpanish: Boolean) {
        useSpanishNotation = useSpanish
        settings.set(KEY_SPANISH_NOTATION, useSpanishNotation)
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        settings.set(KEY_THEME_MODE, mode.name)
    }
}
