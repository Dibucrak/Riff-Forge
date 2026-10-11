package com.riffforge.core.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

sealed class RiffForgeTab : Tab {

    object Tuner : RiffForgeTab() {
        @Composable
        override fun Content() {
            com.riffforge.feature_tuner.presentation.TunerScreen()
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
            com.riffforge.feature_songs.presentation.SongsScreen()
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
            com.riffforge.feature_tools.presentation.ToolsScreen()
        }

        override val options: TabOptions
            @Composable get() = TabOptions(
                index = 2u,
                title = "Herramientas",
                icon = rememberVectorPainter(Icons.Default.Build)
            )
    }

    companion object {
        val List = listOf(Tuner, Songs, Tools)
    }
}
