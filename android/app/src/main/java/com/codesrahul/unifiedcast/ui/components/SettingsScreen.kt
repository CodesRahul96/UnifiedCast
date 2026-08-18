package com.codesrahul.unifiedcast.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesrahul.unifiedcast.ui.theme.*

@Composable
fun FireTvRemoteSettingsScreen(
    useSwipeControl: Boolean,
    isVibrationEnabled: Boolean,
    onSwipeControlChanged: (Boolean) -> Unit,
    onVibrationChanged: (Boolean) -> Unit,
    onShowGuide: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Open App Setup Guide Button Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowGuide() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📖", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "View App Setup Guide",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Learn how to pair TV & use gestures",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Text("OPEN", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AccentCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF262626), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Style Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Navigation Style",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Navigation Style",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Touchpad Gesture Control Option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSwipeControlChanged(true) }
                .padding(start = 38.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Touchpad Control", fontSize = 15.sp, color = Color.White)
            RadioButton(
                selected = useSwipeControl,
                onClick = { onSwipeControlChanged(true) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = FireTvBlue,
                    unselectedColor = Color(0xFF555555)
                )
            )
        }

        // D-Pad Directional Ring Control Option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSwipeControlChanged(false) }
                .padding(start = 38.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("D-Pad Ring Control", fontSize = 15.sp, color = Color.White)
            RadioButton(
                selected = !useSwipeControl,
                onClick = { onSwipeControlChanged(false) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = FireTvBlue,
                    unselectedColor = Color(0xFF555555)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF262626), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Vibration Feedback Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Vibration",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Tactile Vibration", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Switch(
                checked = isVibrationEnabled,
                onCheckedChange = onVibrationChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FireTvBlue,
                    uncheckedThumbColor = Color(0xFF888888),
                    uncheckedTrackColor = Color(0xFF333333)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF262626), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // About & Version Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "About UnifiedCast",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Version", fontSize = 13.sp, color = TextSecondary)
                    Text("v1.0.0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Native Engine", fontSize = 13.sp, color = TextSecondary)
                    Text("v1.0.0-cpp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentEmerald)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Architecture", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
