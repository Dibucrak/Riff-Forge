package com.riffforge.core.domain.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ThemeMode {
    SYSTEM,
    MOONLIGHT_DARK, MOONLIGHT_LIGHT,
    MONOCHROME_DARK, MONOCHROME_LIGHT,
    RETRO_DARK, RETRO_LIGHT
}

/**
 * App-wide settings holder with Compose reactive state + SharedPreferences persistence.
 * Triggers instant recomposition when notation or theme settings change.
 */
object AppSettings {
    private const val PREFS_NAME = "riff_forge_settings"
    private const val KEY_SPANISH_NOTATION = "spanish_notation"
    private const val KEY_THEME_MODE = "theme_mode"

    private var prefs: SharedPreferences? = null

    var useSpanishNotation: Boolean by mutableStateOf(false)
        private set

    var themeMode: ThemeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun init(context: Context) {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp
        useSpanishNotation = sp.getBoolean(KEY_SPANISH_NOTATION, false)
        val savedTheme = sp.getString(KEY_THEME_MODE, null)
        themeMode = if (!savedTheme.isNullOrBlank()) {
            runCatching { ThemeMode.valueOf(savedTheme) }.getOrDefault(ThemeMode.SYSTEM)
        } else {
            ThemeMode.SYSTEM
        }
    }

    fun prefs(context: Context): SharedPreferences =
        prefs ?: context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also {
            prefs = it
        }

    fun toggleSpanishNotation(): Boolean {
        useSpanishNotation = !useSpanishNotation
        prefs?.edit()?.putBoolean(KEY_SPANISH_NOTATION, useSpanishNotation)?.apply()
        return useSpanishNotation
    }

    fun setSpanishNotation(useSpanish: Boolean) {
        useSpanishNotation = useSpanish
        prefs?.edit()?.putBoolean(KEY_SPANISH_NOTATION, useSpanishNotation)?.apply()
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()
    }
}
