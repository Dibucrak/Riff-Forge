package com.riffforge.core.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.riffforge.feature_auth.presentation.login.LoginScreen
import com.riffforge.feature_auth.presentation.register.RegisterScreen
import com.riffforge.feature_metronome.presentation.MetronomeScreen
import com.riffforge.feature_setlists.presentation.setlist_detail.SetlistDetailScreen
import com.riffforge.feature_song_editor.presentation.AddEditSongScreen
import com.riffforge.feature_songs.presentation.SongsScreen
import com.riffforge.feature_theory.presentation.circle_of_fifths.CircleOfFifthsScreen
import com.riffforge.feature_tools.presentation.ToolsScreen
import com.riffforge.feature_tuner.presentation.TunerScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = Modifier.padding(paddingValues)
    ) {

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Tuner.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.Tuner.route) {
            TunerScreen()
        }

        composable(route = Screen.Songs.route) {
            SongsScreen(
                onNavigateToAddSong = {
                    navController.navigate(Screen.AddEditSong.route)
                },
                onNavigateToSetlistDetail = { setId ->
                    navController.navigate(Screen.SetlistDetail.route + "/$setId")
                }
            )
        }

        composable(route = Screen.Tools.route) {
            ToolsScreen(
                onNavigateToCircleOfFifths = {
                    navController.navigate(Screen.CircleOfFifths.route)
                },
                onNavigateToMetronome = {
                    navController.navigate(Screen.Metronome.route)
                }
            )
        }

        composable(route = Screen.AddEditSong.route) {
            AddEditSongScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(
            route = Screen.SetlistDetail.route + "/{setId}",
            arguments = listOf(
                navArgument(name = "setId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            SetlistDetailScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.CircleOfFifths.route) {
            CircleOfFifthsScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.Metronome.route) {
            MetronomeScreen(onNavigateUp = { navController.navigateUp() })
        }
    }
}