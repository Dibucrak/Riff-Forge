package com.riffforge.core.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")

    object Tuner : Screen("tuner_screen")
    object Songs : Screen("songs_screen")
    object Tools : Screen("tools_screen")

    object AddEditSong : Screen("add_edit_song_screen")
    object CircleOfFifths : Screen("circle_of_fifths_screen")
    object Metronome : Screen("metronome_screen")
}