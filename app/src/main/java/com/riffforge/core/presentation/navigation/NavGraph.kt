package com.riffforge.core.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.riffforge.feature_admin.presentation.AdminScreen
import com.riffforge.feature_auth.presentation.login.LoginScreen
import com.riffforge.feature_auth.presentation.register.RegisterScreen
import com.riffforge.feature_chords.presentation.ChordDictionaryScreen
import com.riffforge.feature_contributions.presentation.community_explorer.CommunityScreen
import com.riffforge.feature_ear_training.presentation.EarTrainingScreen
import com.riffforge.feature_metronome.presentation.MetronomeScreen
import com.riffforge.feature_profile.presentation.ProfileScreen
import com.riffforge.feature_setlists.presentation.setlist_detail.SetlistDetailScreen
import com.riffforge.feature_song_editor.presentation.AddEditSongScreen
import com.riffforge.feature_songs.presentation.SongsScreen
import com.riffforge.feature_songs.presentation.song_viewer.SongViewerScreen
import com.riffforge.feature_theory.presentation.circle_of_fifths.CircleOfFifthsScreen
import com.riffforge.feature_tools.presentation.ToolsScreen
import com.riffforge.feature_tuner.presentation.TunerScreen
import com.riffforge.feature_daily_learning.presentation.DailyLearningScreen
import com.riffforge.feature_theory.presentation.progressions.ProgressionScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = Modifier.padding(paddingValues),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
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
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSongViewer = { songId ->
                    navController.navigate(Screen.SongViewer.route + "/$songId")
                }
            )
        }

        composable(route = Screen.Tools.route) {
            ToolsScreen(
                onNavigateToCircleOfFifths = { navController.navigate(Screen.CircleOfFifths.route) },
                onNavigateToMetronome = { navController.navigate(Screen.Metronome.route) },
                onNavigateToChordDictionary = { navController.navigate(Screen.ChordDictionary.route) },
                onNavigateToCommunity = { navController.navigate(Screen.CommunityExplorer.route) },
                onNavigateToEarTraining = { navController.navigate(Screen.EarTraining.route) },
                onNavigateToDailyLearning = { navController.navigate(Screen.DailyLearning.route) },
                onNavigateToProgressions = { navController.navigate(Screen.Progressions.route) },
                onNavigateToScales = { navController.navigate(Screen.Scales.route) }
            )
        }

        composable(route = Screen.DailyLearning.route) {
            DailyLearningScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEarTraining = { navController.navigate(Screen.EarTraining.route) }
            )
        }

        composable(
            route = Screen.AddEditSong.route + "?songId={songId}",
            arguments = listOf(
                navArgument(name = "songId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            AddEditSongScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(
            route = Screen.SongViewer.route + "/{songId}",
            arguments = listOf(
                navArgument(name = "songId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            SongViewerScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEdit = { songId ->
                    navController.navigate(Screen.AddEditSong.route + "?songId=$songId")
                }
            )
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
            SetlistDetailScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToSongViewer = { songId ->
                    navController.navigate(Screen.SongViewer.route + "/$songId")
                }
            )
        }

        composable(route = Screen.CircleOfFifths.route) {
            CircleOfFifthsScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.Metronome.route) {
            MetronomeScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.ChordDictionary.route) {
            ChordDictionaryScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.CommunityExplorer.route) {
            CommunityScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.AdminPanel.route) {
            AdminScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.EarTraining.route) {
            EarTrainingScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = Screen.Progressions.route) {
            ProgressionScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Scales.route) {
            com.riffforge.feature_theory.presentation.scales.ScalesScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToAdminPanel = { navController.navigate(Screen.AdminPanel.route) },
                onSignOutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
