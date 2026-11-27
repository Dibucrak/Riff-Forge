package com.riffforge.core.presentation.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/** Voyager Screen wrappers for top-level tab screens. */

object HomeTabScreen : Screen {
    @Composable
    override fun Content() {
        com.riffforge.feature_home.presentation.HomeScreen()
    }
}

object TunerTabScreen : Screen {
    @Composable
    override fun Content() {
        com.riffforge.feature_tuner.presentation.TunerScreen()
    }
}

object SongsTabScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        com.riffforge.feature_songs.presentation.SongsScreen(navigator = navigator)
    }
}

object ToolsTabScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        com.riffforge.feature_tools.presentation.ToolsScreen(navigator = navigator)
    }
}

object ProfileTabScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        com.riffforge.feature_profile.presentation.ProfileScreen(navigator = navigator)
    }
}
