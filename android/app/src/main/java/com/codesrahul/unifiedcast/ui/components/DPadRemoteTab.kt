package com.codesrahul.unifiedcast.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesrahul.unifiedcast.network.TvKeyCodes
import com.codesrahul.unifiedcast.ui.theme.AccentCyan
import com.codesrahul.unifiedcast.ui.theme.DarkBackground
import com.codesrahul.unifiedcast.ui.theme.SurfaceDark
import com.codesrahul.unifiedcast.ui.theme.TextSecondary

@Composable
fun DPadRemoteTab(
    useTouchpadDefault: Boolean = false,
    onKeyClick: (Int) -> Unit
) {
    var useTouchpad by remember(useTouchpadDefault) { mutableStateOf(useTouchpadDefault) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Mode Switcher Pill (D-PAD vs TOUCHPAD)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (!useTouchpad) AccentCyan else Color.Transparent)
                    .clickable { useTouchpad = false }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("D-PAD", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (!useTouchpad) DarkBackground else Color.White)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (useTouchpad) AccentCyan else Color.Transparent)
                    .clickable { useTouchpad = true }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("TOUCHPAD", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (useTouchpad) DarkBackground else Color.White)
            }
        }

        // System Control Bar (POWER, INPUT, SETTINGS, MUTE)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteSystemButton(label = "POWER", color = Color(0xFF1E1E1E), textColor = Color.Red) {
                onKeyClick(TvKeyCodes.KEYCODE_POWER)
            }
            RemoteSystemButton(label = "INPUT", color = Color(0xFF1E1E1E)) {
                onKeyClick(TvKeyCodes.KEYCODE_TV_INPUT)
            }
            RemoteSystemButton(label = "SETTINGS", color = Color(0xFF1E1E1E)) {
                onKeyClick(TvKeyCodes.KEYCODE_SETTINGS)
            }
            RemoteSystemButton(label = "MUTE", color = Color(0xFF1E1E1E)) {
                onKeyClick(TvKeyCodes.KEYCODE_VOLUME_MUTE)
            }
        }

        // Secondary Navigation Bar (BACK, HOME, MENU, GUIDE)
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteSystemButton(label = "BACK", color = Color(0xFF1E1E1E)) {
                onKeyClick(TvKeyCodes.KEYCODE_BACK)
            }
            RemoteSystemButton(label = "HOME", color = Color(0xFF1E1E1E)) {
                onKeyClick(TvKeyCodes.KEYCODE_HOME)
            }
            RemoteSystemButton(label = "MENU", color = Color(0xFF1E1E1E)) {
                onKeyClick(TvKeyCodes.KEYCODE_MENU)
            }
            RemoteSystemButton(label = "GUIDE", color = Color(0xFF1E1E1E)) {
                onKeyClick(TvKeyCodes.KEYCODE_GUIDE)
            }
        }

        // Navigation Controller Center (D-Pad or Touchpad)
        Box(
            modifier = Modifier.height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            if (useTouchpad) {
                // Fluid Touchpad Surface
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(2.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onKeyClick(TvKeyCodes.KEYCODE_DPAD_CENTER)
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            var accumulatedX = 0f
                            var accumulatedY = 0f
                            val threshold = 35f

                            detectDragGestures(
                                onDragStart = {
                                    accumulatedX = 0f
                                    accumulatedY = 0f
                                },
                                onDragEnd = {
                                    accumulatedX = 0f
                                    accumulatedY = 0f
                                },
                                onDragCancel = {
                                    accumulatedX = 0f
                                    accumulatedY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    accumulatedX += dragAmount.x
                                    accumulatedY += dragAmount.y

                                    val absX = kotlin.math.abs(accumulatedX)
                                    val absY = kotlin.math.abs(accumulatedY)

                                    if (absX > threshold || absY > threshold) {
                                        if (absX > absY) {
                                            if (accumulatedX > threshold) {
                                                onKeyClick(TvKeyCodes.KEYCODE_DPAD_RIGHT)
                                                accumulatedX = 0f
                                                accumulatedY = 0f
                                            } else if (accumulatedX < -threshold) {
                                                onKeyClick(TvKeyCodes.KEYCODE_DPAD_LEFT)
                                                accumulatedX = 0f
                                                accumulatedY = 0f
                                            }
                                        } else {
                                            if (accumulatedY > threshold) {
                                                onKeyClick(TvKeyCodes.KEYCODE_DPAD_DOWN)
                                                accumulatedX = 0f
                                                accumulatedY = 0f
                                            } else if (accumulatedY < -threshold) {
                                                onKeyClick(TvKeyCodes.KEYCODE_DPAD_UP)
                                                accumulatedX = 0f
                                                accumulatedY = 0f
                                            }
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👈 Swipe to Navigate 👉", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap to Select (OK)", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            } else {
                // D-Pad Controller Ring
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E1E))
                        .border(
                            width = 1.5.dp,
                            color = AccentCyan.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                        .shadow(8.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(165.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                    )

                    // UP
                    var isUpPressed by remember { mutableStateOf(false) }
                    val upScale by animateFloatAsState(
                        targetValue = if (isUpPressed) 0.8f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                        label = "Up"
                    )
                    IconButton(
                        onClick = {
                            isUpPressed = true
                            onKeyClick(TvKeyCodes.KEYCODE_DPAD_UP)
                            isUpPressed = false
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .size(48.dp)
                            .scale(upScale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Up",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // DOWN
                    var isDownPressed by remember { mutableStateOf(false) }
                    val downScale by animateFloatAsState(
                        targetValue = if (isDownPressed) 0.8f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                        label = "Down"
                    )
                    IconButton(
                        onClick = {
                            isDownPressed = true
                            onKeyClick(TvKeyCodes.KEYCODE_DPAD_DOWN)
                            isDownPressed = false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .size(48.dp)
                            .scale(downScale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Down",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // LEFT
                    var isLeftPressed by remember { mutableStateOf(false) }
                    val leftScale by animateFloatAsState(
                        targetValue = if (isLeftPressed) 0.8f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                        label = "Left"
                    )
                    IconButton(
                        onClick = {
                            isLeftPressed = true
                            onKeyClick(TvKeyCodes.KEYCODE_DPAD_LEFT)
                            isLeftPressed = false
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .size(48.dp)
                            .scale(leftScale)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Left",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // RIGHT
                    var isRightPressed by remember { mutableStateOf(false) }
                    val rightScale by animateFloatAsState(
                        targetValue = if (isRightPressed) 0.8f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                        label = "Right"
                    )
                    IconButton(
                        onClick = {
                            isRightPressed = true
                            onKeyClick(TvKeyCodes.KEYCODE_DPAD_RIGHT)
                            isRightPressed = false
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(48.dp)
                            .scale(rightScale)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Right",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // CENTER / OK
                    var isOkPressed by remember { mutableStateOf(false) }
                    val okScale by animateFloatAsState(
                        targetValue = if (isOkPressed) 0.85f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                        label = "OK"
                    )
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(okScale)
                            .clip(CircleShape)
                            .background(AccentCyan)
                            .clickable {
                                isOkPressed = true
                                onKeyClick(TvKeyCodes.KEYCODE_DPAD_CENTER)
                                isOkPressed = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OK",
                            color = DarkBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Quick Access Volume (VOL) and Channel (CH) Control Pills on Home/Remote Tab
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // VOL Pill
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.5.dp, AccentCyan.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onKeyClick(TvKeyCodes.KEYCODE_VOLUME_DOWN) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Vol Down", tint = Color.White)
                    }
                    Text(
                        text = "VOL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentCyan,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = { onKeyClick(TvKeyCodes.KEYCODE_VOLUME_UP) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Vol Up", tint = Color.White)
                    }
                }
            }

            // CH Pill
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.5.dp, AccentCyan.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onKeyClick(TvKeyCodes.KEYCODE_CHANNEL_DOWN) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Ch Down", tint = Color.White)
                    }
                    Text(
                        text = "CH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentCyan,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = { onKeyClick(TvKeyCodes.KEYCODE_CHANNEL_UP) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ch Up", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun RemoteSystemButton(
    label: String,
    color: Color = Color(0xFF1E1E1E),
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(72.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = color,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}
