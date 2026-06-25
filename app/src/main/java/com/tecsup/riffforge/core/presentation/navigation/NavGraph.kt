package com.riffforge.core.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.riffforge.feature_song_editor.presentation.AddEditSongScreen
import com.riffforge.feature_songs.presentation.SongsScreen
import com.riffforge.feature_tools.presentation.ToolsScreen
import com.riffforge.feature_tuner.presentation.TunerScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tuner.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(route = Screen.Tuner.route) {
            TunerScreen()
        }
        composable(route = Screen.Songs.route) {
            SongsScreen(
                onNavigateToAddSong = {
                    navController.navigate(Screen.AddEditSong.route)
                }
            )
        }
        composable(route = Screen.Tools.route) {
            ToolsScreen()
        }
        composable(route = Screen.AddEditSong.route) {
            AddEditSongScreen(
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }
    }
}