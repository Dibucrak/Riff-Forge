package com.riffforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.riffforge.core.presentation.components.BottomNavigationBar
import com.riffforge.core.presentation.components.GlassBackground
import com.riffforge.core.presentation.navigation.NavGraph
import com.riffforge.core.presentation.navigation.Screen
import com.riffforge.core.presentation.theme.*
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.isSystemInDarkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val startDestination = if (currentUser != null) {
            Screen.Tuner.route
        } else {
            Screen.Login.route
        }

        setContent {
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (com.riffforge.core.domain.util.AppSettings.themeMode) {
                com.riffforge.core.domain.util.ThemeMode.SYSTEM -> systemDark
                com.riffforge.core.domain.util.ThemeMode.LIGHT -> false
                com.riffforge.core.domain.util.ThemeMode.DARK -> true
            }

            RiffForgeTheme(darkTheme = isDarkTheme) {
                GlassBackground(isDarkTheme = isDarkTheme) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val showBottomBar = currentRoute in listOf(
                        Screen.Tuner.route,
                        Screen.Songs.route,
                        Screen.Tools.route
                    )

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavigationBar(navController = navController, isDarkTheme = isDarkTheme)
                            }
                        }
                    ) { paddingValues ->
                        NavGraph(
                            navController = navController,
                            paddingValues = paddingValues,
                            startDestination = startDestination,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        }
    }
}