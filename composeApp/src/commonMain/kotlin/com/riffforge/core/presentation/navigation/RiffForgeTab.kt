package com.riffforge.core.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition

object TabNavigationTracker {
    var isCurrentTabAtRoot by mutableStateOf(true)
    var currentTabNavigator: Navigator? = null

    fun popCurrentToRoot() {
        currentTabNavigator?.popUntilRoot()
        isCurrentTabAtRoot = true
    }
}

sealed class RiffForgeTab : Tab {

    object Tuner : RiffForgeTab() {
        @Composable
        override fun Content() {
            Navigator(TunerTabScreen) { navigator ->
                SideEffect {
                    TabNavigationTracker.currentTabNavigator = navigator
                    TabNavigationTracker.isCurrentTabAtRoot = navigator.size <= 1
                }
                SlideTransition(navigator)
            }
        }

        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 0u,
                title = "Afinador",
                icon = rememberVectorPainter(Icons.Default.Mic)
            )
    }

    object Songs : RiffForgeTab() {
        @Composable
        override fun Content() {
            Navigator(SongsTabScreen) { navigator ->
                SideEffect {
                    TabNavigationTracker.currentTabNavigator = navigator
                    TabNavigationTracker.isCurrentTabAtRoot = navigator.size <= 1
                }
                SlideTransition(navigator)
            }
        }

        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 1u,
                title = "Canciones",
                icon = rememberVectorPainter(Icons.Default.LibraryMusic)
            )
    }

    object Tools : RiffForgeTab() {
        @Composable
        override fun Content() {
            Navigator(ToolsTabScreen) { navigator ->
                SideEffect {
                    TabNavigationTracker.currentTabNavigator = navigator
                    TabNavigationTracker.isCurrentTabAtRoot = navigator.size <= 1
                }
                SlideTransition(navigator)
            }
        }

        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 2u,
                title = "Herramientas",
                icon = rememberVectorPainter(Icons.Default.Build)
            )
    }

    object Home : RiffForgeTab() {
        @Composable
        override fun Content() {
            Navigator(HomeTabScreen) { navigator ->
                SideEffect {
                    TabNavigationTracker.currentTabNavigator = navigator
                    TabNavigationTracker.isCurrentTabAtRoot = navigator.size <= 1
                }
                SlideTransition(navigator)
            }
        }

        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 3u,
                title = "Inicio",
                icon = rememberVectorPainter(Icons.Default.Home)
            )
    }

    object Profile : RiffForgeTab() {
        @Composable
        override fun Content() {
            Navigator(ProfileTabScreen) { navigator ->
                SideEffect {
                    TabNavigationTracker.currentTabNavigator = navigator
                    TabNavigationTracker.isCurrentTabAtRoot = navigator.size <= 1
                }
                SlideTransition(navigator)
            }
        }

        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 4u,
                title = "Perfil",
                icon = rememberVectorPainter(Icons.Default.Person)
            )
    }

    companion object {
        val List = listOf(Tuner, Songs, Tools)
    }
}
