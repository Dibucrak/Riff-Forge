package com.riffforge.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.riffforge.core.presentation.navigation.RiffForgeTab
import com.riffforge.core.presentation.theme.*

/**
 * Liquid Glass Tab Bar — floating capsule, following the iOS tab bar guidance.
 *
 * Visual effect:
 *   - Thin translucent glass capsule floating above content, inset from the screen edges
 *   - Specular rim highlight and a soft neutral shadow
 *   - Selected tab: tint-colored icon + label over a subtle glass "lens" pill
 *   - Unselected tabs use secondary label color; color is never used for decoration
 */
@Composable
fun BottomNavigationBar(
    tabNavigator: TabNavigator
) {
    val tabs = RiffForgeTab.List
    val currentTab = tabNavigator.current
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassBackgroundBase)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    spotColor = GlassShadowElevated,
                    ambientColor = GlassShadowElevated
                ),
            shape = shape,
            color = GlassSurfaceThin,
            border = BorderStroke(width = 0.5.dp, brush = glassRimBrush())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    val itemColor = if (isSelected) AccentTint else TextOnGlassSecondary

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                color = if (isSelected) GlassSurfaceDim else Color.Transparent,
                                shape = RoundedCornerShape(26.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (currentTab != tab) {
                                        tabNavigator.current = tab
                                    }
                                }
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        tab.options.icon?.let { painter ->
                            Icon(
                                painter = painter,
                                contentDescription = tab.options.title,
                                tint = itemColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = tab.options.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = itemColor,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
