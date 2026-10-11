package com.riffforge.core.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.riffforge.core.presentation.navigation.RiffForgeTab

@Composable
fun BottomNavigationBar(
    tabNavigator: TabNavigator
) {
    val tabs = RiffForgeTab.List
    val currentTab = tabNavigator.current

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                icon = {
                    tab.options.icon?.let { painter ->
                        Icon(painter = painter, contentDescription = tab.options.title)
                    }
                },
                label = { Text(text = tab.options.title) },
                selected = currentTab == tab,
                onClick = {
                    if (currentTab != tab) {
                        tabNavigator.current = tab
                    }
                }
            )
        }
    }
}
