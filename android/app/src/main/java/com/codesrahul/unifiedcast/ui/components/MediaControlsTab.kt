package com.codesrahul.unifiedcast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesrahul.unifiedcast.network.TvKeyCodes
import com.codesrahul.unifiedcast.ui.theme.AccentCyan
import com.codesrahul.unifiedcast.ui.theme.SurfaceDark

@Composable
fun MediaControlsTab(
    onKeyClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "MEDIA & VOLUME CONTROL CENTER",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = AccentCyan,
            letterSpacing = 1.sp
        )

        // Primary Playback Controls Row (Rewind, Play/Pause, FastForward)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaControlButton(
                icon = Icons.Default.FastRewind,
                label = "REWIND"
            ) {
                onKeyClick(TvKeyCodes.KEYCODE_MEDIA_REWIND)
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AccentCyan)
                    .clickable { onKeyClick(TvKeyCodes.KEYCODE_MEDIA_PLAY_PAUSE) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            MediaControlButton(
                icon = Icons.Default.FastForward,
                label = "FORWARD"
            ) {
                onKeyClick(TvKeyCodes.KEYCODE_MEDIA_FAST_FORWARD)
            }
        }

        // Secondary Control Row (Previous Track & Next Track)
        Row(
            modifier = Modifier.fillMaxWidth(0.7f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaControlButton(
                icon = Icons.Default.SkipPrevious,
                label = "PREV"
            ) {
                onKeyClick(TvKeyCodes.KEYCODE_MEDIA_PREVIOUS)
            }

            MediaControlButton(
                icon = Icons.Default.SkipNext,
                label = "NEXT"
            ) {
                onKeyClick(TvKeyCodes.KEYCODE_MEDIA_NEXT)
            }
        }

        // Channel & Volume Control Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Volume Controls Pill
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { onKeyClick(TvKeyCodes.KEYCODE_VOLUME_UP) }) {
                        Icon(Icons.Default.Add, contentDescription = "Vol Up", tint = Color.White)
                    }
                    Text("VOL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    IconButton(onClick = { onKeyClick(TvKeyCodes.KEYCODE_VOLUME_DOWN) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Vol Down", tint = Color.White)
                    }
                }
            }

            // Mute Center Stack
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MediaControlButton(
                    icon = Icons.AutoMirrored.Filled.VolumeOff,
                    label = "MUTE"
                ) {
                    onKeyClick(TvKeyCodes.KEYCODE_VOLUME_MUTE)
                }
            }

            // Channel Controls Pill
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { onKeyClick(TvKeyCodes.KEYCODE_CHANNEL_UP) }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ch Up", tint = Color.White)
                    }
                    Text("CH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    IconButton(onClick = { onKeyClick(TvKeyCodes.KEYCODE_CHANNEL_DOWN) }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Ch Down", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MediaControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = SurfaceDark
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
