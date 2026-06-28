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
    object ChordDictionary : Screen("chord_dictionary_screen")
    object CommunityExplorer : Screen("community_explorer_screen")
    object AdminPanel : Screen("admin_panel_screen")
    object EarTraining : Screen("ear_training_screen")
    object DailyLearning : Screen("daily_learning_screen")

    object SetlistDetail : Screen("setlist_detail_screen")
    object Profile : Screen("profile_screen")
    object SongViewer : Screen("song_viewer_screen")
}