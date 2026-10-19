package com.riffforge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.riffforge.core.presentation.components.BottomNavigationBar
import com.riffforge.core.presentation.navigation.RiffForgeTab
import com.riffforge.core.presentation.theme.RiffForgeTheme

@Composable
fun App() {
    RiffForgeTheme {
        TabNavigator(tab = RiffForgeTab.Home) { tabNavigator ->
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(tabNavigator = tabNavigator)
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    CurrentTab()
                }
            }
        }
    }
}
