package com.codesrahul.unifiedcast.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.codesrahul.unifiedcast.ui.theme.AccentCyan

@Composable
fun FloatingBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val navBg = Color(0xFF1E1E1E)

    val tabs = listOf(
        Triple(2, "Media", Icons.Default.PlayArrow),
        Triple(1, "Apps", Icons.Default.Menu),
        Triple(0, "Remote", Icons.Default.Home),
        Triple(3, "Keyboard", Icons.Default.Create),
        Triple(4, "Settings", Icons.Default.Settings)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(36.dp))
                .border(1.5.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(36.dp)),
            color = navBg,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (index, label, icon) ->
                    val selected = selectedTab == index
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.15f else 1.0f,
                        label = "IconScale"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (selected) AccentCyan.copy(alpha = 0.2f) else Color.Transparent
                            )
                            .border(
                                width = if (selected) 1.dp else 0.dp,
                                color = if (selected) AccentCyan.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(index) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selected) AccentCyan else Color(0xFF94A3B8),
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )

                            if (selected) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(AccentCyan)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
