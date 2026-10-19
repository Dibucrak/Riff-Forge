package com.riffforge.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.riffforge.core.presentation.navigation.Screen
import com.riffforge.core.presentation.theme.*

@Composable
fun BottomNavigationBar(
    navController: NavController,
    isDarkTheme: Boolean
) {
    val items = listOf(
        BottomNavItem(
            title = "Afinador",
            route = Screen.Tuner.route,
            icon = Icons.Default.Mic
        ),
        BottomNavItem(
            title = "Canciones",
            route = Screen.Songs.route,
            icon = Icons.Default.LibraryMusic
        ),
        BottomNavItem(
            title = "Herramientas",
            route = Screen.Tools.route,
            icon = Icons.Default.Build
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val backgroundColor = if (isDarkTheme) GlassSurfaceThinDark else GlassSurfaceThinLight
    val selectedFill = if (isDarkTheme) GlassSurfaceDarkDim else GlassSurfaceLightDim
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight
    val shape = RoundedCornerShape(32.dp)

    // Floating Liquid Glass tab bar (capsule inset from the screen edges)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    spotColor = GlassShadowElevated,
                    ambientColor = GlassShadowElevated
                ),
            shape = shape,
            color = backgroundColor,
            border = BorderStroke(width = 0.5.dp, brush = glassRimBrush(isDarkTheme))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val itemColor = if (isSelected) AccentTint else dimTextColor

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                color = if (isSelected) selectedFill else Color.Transparent,
                                shape = RoundedCornerShape(26.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = itemColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = itemColor
                        )
                    }
                }
            }
        }
    }
}
