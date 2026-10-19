package com.riffforge.core.domain.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/**
 * App-wide settings holder with Compose reactive state.
 * Triggers instant recomposition when notation or theme settings change.
 */
object AppSettings {
    var useSpanishNotation: Boolean by mutableStateOf(false)
        private set

    var themeMode: ThemeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun toggleSpanishNotation(): Boolean {
        useSpanishNotation = !useSpanishNotation
        return useSpanishNotation
    }

    fun setSpanishNotation(useSpanish: Boolean) {
        useSpanishNotation = useSpanish
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
    }
}
