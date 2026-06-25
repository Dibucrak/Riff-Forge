package com.riffforge.core.presentation.navigation

sealed class Screen(val route: String) {
    object Tuner : Screen("tuner_screen")
    object Songs : Screen("songs_screen")
    object Tools : Screen("tools_screen")
}