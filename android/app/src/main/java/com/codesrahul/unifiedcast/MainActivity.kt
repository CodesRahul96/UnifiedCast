package com.codesrahul.unifiedcast

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesrahul.unifiedcast.network.*
import com.codesrahul.unifiedcast.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    init {
        System.loadLibrary("unifiedcast_native")
    }

    private external fun getNativeEngineVersion(): String

    private val tvClient = TvRemoteClient()
    private var tvDiscovery: TvDiscovery? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock App strictly in Vertical (Portrait) Orientation
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Configure Status Bar and Navigation Bar colors
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        setContent {
            UnifiedCastTheme {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()

                val isDark = true
                val bg = DarkBackground
                val surface = SurfaceDark
                val cardBg = CardBackground
                val txtPrimary = TextPrimary
                val txtSecondary = TextSecondary

                val prefs = remember { context.getSharedPreferences("unifiedcast_prefs", Context.MODE_PRIVATE) }
                var showSplash by remember { mutableStateOf(true) }
                var showUserManual by remember { mutableStateOf(prefs.getBoolean("is_first_time", true)) }

                val irManager = remember { com.codesrahul.unifiedcast.hardware.IrRemoteManager(context) }

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1800)
                    showSplash = false
                }

                var isConnected by remember { mutableStateOf(false) }
                var isVibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
                var useSwipeControl by remember { mutableStateOf(prefs.getBoolean("swipe_control", false)) }
                var connectionStatus by remember { mutableStateOf("Searching Smart TVs on Wi-Fi...") }
                var tvIpAddress by remember { mutableStateOf(prefs.getString("last_tv_ip", "192.168.9.246") ?: "192.168.9.246") }
                var activeDeviceName by remember { mutableStateOf(prefs.getString("last_tv_name", "Smart TV") ?: "Smart TV") }
                var selectedTab by remember { mutableStateOf(0) }
                var showPairDialog by remember { mutableStateOf(false) }
                var isScanning by remember { mutableStateOf(false) }
                val discoveredDevices = remember { mutableStateListOf<DiscoveredDevice>() }
                val nativeVersion = remember { getNativeEngineVersion() }

                fun connectToDevice(device: DiscoveredDevice) {
                    tvIpAddress = device.ip
                    activeDeviceName = device.name
                    prefs.edit()
                        .putString("last_tv_ip", device.ip)
                        .putString("last_tv_name", device.name)
                        .apply()
                    coroutineScope.launch {
                        connectionStatus = "Connecting to ${device.name} (${device.ip})..."
                        val success = tvClient.connect(device.ip, device.port)
                        isConnected = success
                        connectionStatus = if (success) "Connected to ${device.name}" else "Failed to connect to ${device.ip}"
                    }
                }

                // Firebase Remote Config & Analytics Initialization
                var remoteAppVersion by remember { mutableStateOf("v1.0.0") }
                val remoteConfig = remember { com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance() }

                LaunchedEffect(Unit) {
                    try {
                        val configSettings = com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder()
                            .setMinimumFetchIntervalInSeconds(3600)
                            .build()
                        remoteConfig.setConfigSettingsAsync(configSettings)
                        remoteConfig.setDefaultsAsync(mapOf("latest_app_version" to "1.0.0"))
                        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val fetchedVersion = remoteConfig.getString("latest_app_version")
                                if (fetchedVersion.isNotBlank()) {
                                    remoteAppVersion = "v$fetchedVersion"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Auto-connect to last saved device on app startup
                LaunchedEffect(Unit) {
                    val savedIp = prefs.getString("last_tv_ip", null)
                    val savedName = prefs.getString("last_tv_name", "Smart TV") ?: "Smart TV"
                    if (!savedIp.isNullOrEmpty()) {
                        val lastDev = DiscoveredDevice(name = savedName, ip = savedIp, port = 5555)
                        connectToDevice(lastDev)
                    }
                }

                fun startSubnetPairScan() {
                    isScanning = true
                    discoveredDevices.clear()
                    coroutineScope.launch {
                        val scanner = SubnetScanner(context)
                        scanner.scanLocalWifiSubnet { dev ->
                            if (discoveredDevices.none { it.ip == dev.ip }) {
                                discoveredDevices.add(dev)
                            }
                        }
                        isScanning = false
                    }
                }

                LaunchedEffect(Unit) {
                    tvDiscovery = TvDiscovery(this@MainActivity) { host, port, name ->
                        val dev = DiscoveredDevice(name = name, ip = host, port = port)
                        if (discoveredDevices.none { it.ip == host }) {
                            discoveredDevices.add(dev)
                        }
                        if (!isConnected) {
                            connectToDevice(dev)
                        }
                    }
                    tvDiscovery?.startDiscovery()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = bg
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Header Bar with Pair Button & Disconnect functionality
                        TopHeader(
                            status = connectionStatus,
                            isConnected = isConnected,
                            deviceName = activeDeviceName,
                            nativeVersion = nativeVersion,
                            isDark = isDark,
                            onPairClick = {
                                showPairDialog = true
                                startSubnetPairScan()
                            },
                            onDisconnectClick = {
                                tvClient.disconnect()
                                isConnected = false
                                connectionStatus = "Disconnected"
                                activeDeviceName = "Smart TV"
                            }
                        )

                        // Animated Main Content Container with Swipe Gesture Navigation
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(surface.copy(alpha = 0.85f))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f), RoundedCornerShape(24.dp))
                                .pointerInput(selectedTab) {
                                    var totalDrag = 0f
                                    val tabOrder = listOf(2, 1, 0, 3, 4)
                                    detectHorizontalDragGestures(
                                        onDragStart = { totalDrag = 0f },
                                        onDragEnd = {
                                            val currentIndex = tabOrder.indexOf(selectedTab)
                                            if (totalDrag < -80f && currentIndex < tabOrder.lastIndex) {
                                                selectedTab = tabOrder[currentIndex + 1]
                                            } else if (totalDrag > 80f && currentIndex > 0) {
                                                selectedTab = tabOrder[currentIndex - 1]
                                            }
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            totalDrag += dragAmount
                                        }
                                    )
                                }
                                .padding(12.dp)
                        ) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                            slideOutHorizontally { width -> -width } + fadeOut()
                                        )
                                    } else {
                                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                            slideOutHorizontally { width -> width } + fadeOut()
                                        )
                                    }.using(SizeTransform(clip = false))
                                },
                                label = "TabTransition"
                            ) { tab ->
                                when (tab) {
                                    0 -> DPadRemoteTab(
                                        useTouchpadDefault = useSwipeControl,
                                        onKeyClick = { code ->
                                            vibrate(vibrator, isVibrationEnabled)
                                            irManager.transmitKey(code)
                                            coroutineScope.launch { tvClient.sendKeyEvent(code) }
                                        }
                                    )
                                    1 -> TvAppLauncherTab(
                                        isDark = isDark,
                                        onLaunchApp = { packageName ->
                                            coroutineScope.launch {
                                                vibrate(vibrator, isVibrationEnabled)
                                                tvClient.launchApp(packageName)
                                            }
                                        }
                                    )
                                    2 -> MediaControlsTab(
                                        onKeyClick = { code ->
                                            vibrate(vibrator, isVibrationEnabled)
                                            coroutineScope.launch { tvClient.sendKeyEvent(code) }
                                        }
                                    )
                                    3 -> TvTextInputTab(
                                        onSendText = { text ->
                                            vibrate(vibrator, isVibrationEnabled)
                                            coroutineScope.launch { tvClient.sendTextInput(text) }
                                        }
                                    )
                                    4 -> FireTvRemoteSettingsScreen(
                                        useSwipeControl = useSwipeControl,
                                        isVibrationEnabled = isVibrationEnabled,
                                        onSwipeControlChanged = { enabled ->
                                            useSwipeControl = enabled
                                            prefs.edit().putBoolean("swipe_control", enabled).apply()
                                        },
                                        onVibrationChanged = { enabled ->
                                            isVibrationEnabled = enabled
                                            prefs.edit().putBoolean("vibration_enabled", enabled).apply()
                                        }
                                    )
                                }
                            }
                        }

                        // Custom Floating Cyberpunk Bottom Navigation Bar
                        FloatingBottomNavBar(
                            selectedTab = selectedTab,
                            isDark = isDark,
                            onTabSelected = { selectedTab = it }
                        )
                    }

                    // Pair Devices Bottom Sheet Dialog
                    if (showPairDialog) {
                        PairDevicesDialog(
                            isScanning = isScanning,
                            devices = discoveredDevices,
                            onDeviceSelect = { dev ->
                                connectToDevice(dev)
                                showPairDialog = false
                            },
                            onRefreshScan = { startSubnetPairScan() },
                            onDismiss = { showPairDialog = false }
                        )
                    }

                    // First-Time User Setup & Manual Modal
                    if (showUserManual) {
                        UserManualModalDialog(
                            onGetStarted = {
                                prefs.edit().putBoolean("is_first_time", false).apply()
                                showUserManual = false
                            }
                        )
                    }

                    // Animated App Launch Splash Screen
                    if (showSplash) {
                        SplashScreenOverlay()
                    }
                }
            }
        }
    }

    private fun vibrate(vibrator: Vibrator, isVibrationEnabled: Boolean = true) {
        if (!isVibrationEnabled) return
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (e: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        tvClient.disconnect()
    }
}

@Composable
fun FloatingBottomNavBar(
    selectedTab: Int,
    isDark: Boolean,
    onTabSelected: (Int) -> Unit
) {
    val navBg = Color(0xFF1E1E1E)
    val txtSec = Color(0xFF94A3B8)

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
                .clip(RoundedCornerShape(36.dp)),
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

                    Box(
                        modifier = Modifier
                            .size(if (selected) 54.dp else 44.dp)
                            .clip(CircleShape)
                            .background(if (selected) Color(0xFFE5E5E5) else Color.Transparent)
                            .clickable { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) Color(0xFF121212) else txtSec,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeader(
    status: String,
    isConnected: Boolean,
    deviceName: String,
    nativeVersion: String,
    isDark: Boolean,
    onPairClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    val navBgColor = Color(0xFF1E1E1E)
    val textCol = TextPrimary
    val txtSec = TextSecondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = navBgColor),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding Title Card (Clean title only)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "UnifiedCast",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = textCol,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Center Status Indicator Card / Pill
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) AccentEmerald.copy(alpha = 0.15f) else Color(0xFF262626)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isConnected) AccentEmerald.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) AccentEmerald else AccentRose)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "ONLINE" else "OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isConnected) AccentEmerald else AccentRose,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Dynamic Dual-State Paired Device Action Card
            if (isConnected) {
                // Connected Paired Card with Device Info & Disconnect Button
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentEmerald.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald)
                                .clickable { onPairClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📺", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = deviceName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textCol,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = 90.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Red Disconnect Chip Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentRose.copy(alpha = 0.85f))
                                .clickable { onDisconnectClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DISCONNECT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // PAIR DEVICE Action Card
                Card(
                    modifier = Modifier.clickable { onPairClick() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Pair",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PAIR DEVICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairDevicesDialog(
    isScanning: Boolean,
    devices: List<DiscoveredDevice>,
    onDeviceSelect: (DiscoveredDevice) -> Unit,
    onRefreshScan: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Available Devices",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Select a Smart TV on your local Wi-Fi",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onRefreshScan,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = isScanning, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardBackground.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentCyan, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Scanning local Wi-Fi network...", fontSize = 12.sp, color = TextSecondary)
                }
            }

            if (devices.isEmpty() && !isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Smart TVs found. Ensure TV & Phone are on same Wi-Fi with Wireless ADB enabled.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(
                        items = devices,
                        key = { it.ip }
                    ) { device ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(spring(stiffness = 300f)) + slideInVertically(initialOffsetY = { 40 }) + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                    .clickable { onDeviceSelect(device) },
                                color = CardBackground,
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(AccentCyan.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Build,
                                                contentDescription = null,
                                                tint = AccentCyan,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = device.name.substringBefore("(").trim(),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(AccentEmerald)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${device.ip}:${device.port}",
                                                    fontSize = 12.sp,
                                                    color = AccentCyan,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { onDeviceSelect(device) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            text = "CONNECT",
                                            color = DarkBackground,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DPadRemoteTab(
    isDark: Boolean = true,
    useTouchpadDefault: Boolean = false,
    onKeyClick: (Int) -> Unit
) {
    var useTouchpad by remember(useTouchpadDefault) { mutableStateOf(useTouchpadDefault) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Mode Switcher Pill (D-PAD vs TOUCHPAD)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (!useTouchpad) AccentCyan else Color.Transparent)
                    .clickable { useTouchpad = false }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("D-PAD", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (!useTouchpad) DarkBackground else Color.White)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (useTouchpad) AccentCyan else Color.Transparent)
                    .clickable { useTouchpad = true }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("TOUCHPAD", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (useTouchpad) DarkBackground else Color.White)
            }
        }

        // System Control Bar (POWER, INPUT, SETTINGS, INFO)
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteSystemButton(label = "POWER", color = Color(0xFF1E1E1E), textColor = Color.White, isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_POWER)
            }
            RemoteSystemButton(label = "INPUT", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_TV_INPUT)
            }
            RemoteSystemButton(label = "SETTINGS", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_SETTINGS)
            }
            RemoteSystemButton(label = "INFO", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_INFO)
            }
        }

        // Secondary Navigation Bar (BACK, HOME, MENU, GUIDE)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteSystemButton(label = "BACK", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_BACK)
            }
            RemoteSystemButton(label = "HOME", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_HOME)
            }
            RemoteSystemButton(label = "MENU", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_MENU)
            }
            RemoteSystemButton(label = "GUIDE", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_GUIDE)
            }
        }

        if (useTouchpad) {
            // Fluid Glassmorphic Touchpad Surface
            Box(
                modifier = Modifier
                    .size(270.dp)
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
                    Text("👈 Swipe to Navigate 👉", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap to Select (OK)", fontSize = 10.sp, color = TextSecondary)
                }
            }
        } else {

        // Ultra-Modern D-Pad Controller Ring Matching Navigation Pill
        Box(
            modifier = Modifier
                .size(270.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1E1E))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.10f),
                    shape = CircleShape
                )
                .shadow(12.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner Translucent Guide Ring
            Box(
                modifier = Modifier
                    .size(195.dp)
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
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .size(72.dp)
                    .scale(upScale)
                    .clip(CircleShape)
                    .background(
                        if (isUpPressed) Color.White.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .clickable {
                        isUpPressed = true
                        onKeyClick(TvKeyCodes.KEYCODE_DPAD_UP)
                    },
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(isUpPressed) { if (isUpPressed) { kotlinx.coroutines.delay(100); isUpPressed = false } }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "UP",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            // DOWN
            var isDownPressed by remember { mutableStateOf(false) }
            val downScale by animateFloatAsState(
                targetValue = if (isDownPressed) 0.8f else 1.0f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                label = "Down"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .size(72.dp)
                    .scale(downScale)
                    .clip(CircleShape)
                    .background(
                        if (isDownPressed) Color.White.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .clickable {
                        isDownPressed = true
                        onKeyClick(TvKeyCodes.KEYCODE_DPAD_DOWN)
                    },
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(isDownPressed) { if (isDownPressed) { kotlinx.coroutines.delay(100); isDownPressed = false } }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "DOWN",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            // LEFT
            var isLeftPressed by remember { mutableStateOf(false) }
            val leftScale by animateFloatAsState(
                targetValue = if (isLeftPressed) 0.8f else 1.0f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                label = "Left"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .size(72.dp)
                    .scale(leftScale)
                    .clip(CircleShape)
                    .background(
                        if (isLeftPressed) Color.White.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .clickable {
                        isLeftPressed = true
                        onKeyClick(TvKeyCodes.KEYCODE_DPAD_LEFT)
                    },
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(isLeftPressed) { if (isLeftPressed) { kotlinx.coroutines.delay(100); isLeftPressed = false } }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "LEFT",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            // RIGHT
            var isRightPressed by remember { mutableStateOf(false) }
            val rightScale by animateFloatAsState(
                targetValue = if (isRightPressed) 0.8f else 1.0f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                label = "Right"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(72.dp)
                    .scale(rightScale)
                    .clip(CircleShape)
                    .background(
                        if (isRightPressed) Color.White.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .clickable {
                        isRightPressed = true
                        onKeyClick(TvKeyCodes.KEYCODE_DPAD_RIGHT)
                    },
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(isRightPressed) { if (isRightPressed) { kotlinx.coroutines.delay(100); isRightPressed = false } }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "RIGHT",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            // CLEAN MATTE OK BUTTON MATCHING NAV CAPSULE
            var isOkPressed by remember { mutableStateOf(false) }
            val okScale by animateFloatAsState(
                targetValue = if (isOkPressed) 0.85f else 1.0f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                label = "Ok"
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(okScale)
                    .clip(CircleShape)
                    .background(
                        if (isOkPressed) Color(0xFFE5E5E5) else Color(0xFF2A2A2A)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .shadow(8.dp, CircleShape)
                    .clickable {
                        isOkPressed = true
                        onKeyClick(TvKeyCodes.KEYCODE_DPAD_CENTER)
                    },
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(isOkPressed) { if (isOkPressed) { kotlinx.coroutines.delay(100); isOkPressed = false } }
                Text(
                    text = "OK",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isOkPressed) Color(0xFF121212) else Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }

        // Channel Rockers (CH +, CH -)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onKeyClick(TvKeyCodes.KEYCODE_CHANNEL_UP) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
            ) {
                Text("CH ▲", fontWeight = FontWeight.ExtraBold, color = AccentCyan)
            }

            Button(
                onClick = { onKeyClick(TvKeyCodes.KEYCODE_CHANNEL_DOWN) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
            ) {
                Text("CH ▼", fontWeight = FontWeight.ExtraBold, color = AccentCyan)
            }
        }
    }
}

@Composable
fun RemoteSystemButton(
    label: String,
    color: Color = Color(0xFF1E1E1E),
    textColor: Color = TextPrimary,
    isDark: Boolean = true,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "BtnScale"
    )

    val btnBg = if (isDark) color else LightCardBackground
    val btnText = if (isDark) textColor else LightTextPrimary
    val borderCol = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f)

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isPressed) Color(0xFFE5E5E5)
                else btnBg
            )
            .border(1.dp, borderCol, CircleShape)
            .shadow(if (isPressed) 2.dp else 6.dp, CircleShape)
            .clickable {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isPressed) Color(0xFF121212) else btnText,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun MediaControlsTab(
    isDark: Boolean = true,
    onKeyClick: (Int) -> Unit
) {
    val textCol = if (isDark) TextPrimary else LightTextPrimary
    val txtSec = if (isDark) TextSecondary else LightTextSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Section Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Media Controls",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = textCol,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Playback, Volume & Channel Control",
                fontSize = 11.sp,
                color = txtSec,
                fontWeight = FontWeight.Bold
            )
        }

        // Center Play / Pause Focal Button (matching Home D-Pad OK center ring aesthetic)
        var isPlayPressed by remember { mutableStateOf(false) }
        val playScale by animateFloatAsState(
            targetValue = if (isPlayPressed) 0.85f else 1.0f,
            animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
            label = "PlayScale"
        )

        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(playScale)
                .clip(CircleShape)
                .background(if (isPlayPressed) Color(0xFFE5E5E5) else Color(0xFF1E1E1E))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = CircleShape
                )
                .shadow(8.dp, CircleShape)
                .clickable {
                    isPlayPressed = true
                    onKeyClick(TvKeyCodes.KEYCODE_MEDIA_PLAY_PAUSE)
                },
            contentAlignment = Alignment.Center
        ) {
            LaunchedEffect(isPlayPressed) { if (isPlayPressed) { kotlinx.coroutines.delay(100); isPlayPressed = false } }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = if (isPlayPressed) Color(0xFF121212) else Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        // Transport Controls Row (REW, PREV, NEXT, FWD) - circular matching Home tab
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteSystemButton(label = "REW", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_MEDIA_REWIND)
            }
            RemoteSystemButton(label = "PREV", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_MEDIA_PREVIOUS)
            }
            RemoteSystemButton(label = "NEXT", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_MEDIA_NEXT)
            }
            RemoteSystemButton(label = "FWD", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_MEDIA_FAST_FORWARD)
            }
        }

        // Volume Controls Row (VOL-, MUTE, VOL+)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteSystemButton(label = "VOL -", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_VOLUME_DOWN)
            }
            RemoteSystemButton(label = "MUTE", color = AccentRose.copy(alpha = 0.85f), textColor = Color.White, isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_VOLUME_MUTE)
            }
            RemoteSystemButton(label = "VOL +", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_VOLUME_UP)
            }
        }

        // Channel Rockers Row (CH -, CH +)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteSystemButton(label = "CH -", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_CHANNEL_DOWN)
            }
            RemoteSystemButton(label = "CH +", color = Color(0xFF1E1E1E), isDark = isDark) {
                onKeyClick(TvKeyCodes.KEYCODE_CHANNEL_UP)
            }
        }
    }
}

@Composable
fun TvTextInputTab(onSendText: (String) -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Smart TV Universal Keyboard", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AccentCyan)

        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Type text to send to TV screen") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            maxLines = 4
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    val clip = clipboardManager.getText()?.text ?: ""
                    textInput = clip
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
            ) {
                Text("Paste Clipboard", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (textInput.isNotEmpty()) {
                        onSendText(textInput)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("Type on TV", color = DarkBackground, fontWeight = FontWeight.Black)
            }
        }
    }
}

data class TvAppItem(
    val name: String,
    val packageName: String,
    val iconSymbol: String,
    val brandGradient: List<Color>,
    val iconUrl: String? = null
)

@Composable
fun TvAppLauncherTab(
    isDark: Boolean = true,
    onLaunchApp: (String) -> Unit
) {
    val context = LocalContext.current
    val cardBg = if (isDark) CardBackground else LightCardBackground
    val textCol = if (isDark) TextPrimary else LightTextPrimary
    val borderCol = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)

    val timeZoneId = java.util.TimeZone.getDefault().id
    val isIndianRegion = timeZoneId.contains("Kolkata", ignoreCase = true) || timeZoneId.contains("Asia/Calcutta", ignoreCase = true) || timeZoneId.contains("IST", ignoreCase = true)

    val indianApps = listOf(
        TvAppItem("JioCinema", "com.jio.media.ondemand", "J", listOf(Color(0xFFD80075), Color(0xFF800045)), "https://assets.stickpng.com/images/64f0b2f567b578c7c97fbe13.png"),
        TvAppItem("Disney+ Hotstar", "in.startv.hotstar", "★", listOf(Color(0xFF113CCF), Color(0xFF0B1B6D)), "https://upload.wikimedia.org/wikipedia/commons/1/1e/Disney%2B_Hotstar_logo.png"),
        TvAppItem("Zee5", "com.graymatrix.did", "Z", listOf(Color(0xFF8B008B), Color(0xFF4B0082)), "https://upload.wikimedia.org/wikipedia/commons/5/5a/ZEE5_logo.png"),
        TvAppItem("SonyLIV", "com.sonyliv", "S", listOf(Color(0xFFFF8C00), Color(0xFFD2691E)), "https://upload.wikimedia.org/wikipedia/commons/e/e1/SonyLIV_logo.png"),
        TvAppItem("YouTube", "com.google.android.youtube.tv", "▶", listOf(Color(0xFFFF0000), Color(0xFF990000)), "https://upload.wikimedia.org/wikipedia/commons/e/ef/Youtube_logo.png"),
        TvAppItem("Netflix", "com.netflix.ninja", "N", listOf(Color(0xFFE50914), Color(0xFF830A0F)), "https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_N_logo.svg"),
        TvAppItem("Prime Video", "com.amazon.amazonvideo.livingroom", "P", listOf(Color(0xFF00A8E1), Color(0xFF005B7F)), "https://upload.wikimedia.org/wikipedia/commons/f/f1/Prime_Video.png"),
        TvAppItem("Sun NXT", "com.suntv.sunnxt", "☀", listOf(Color(0xFFFF4500), Color(0xFF8B0000))),
        TvAppItem("Aha", "in.aha.android.tv", "a", listOf(Color(0xFFFF5722), Color(0xFFBF360C))),
        TvAppItem("Spotify", "com.spotify.tv.android", "♫", listOf(Color(0xFF1DB954), Color(0xFF0F5D2A)), "https://upload.wikimedia.org/wikipedia/commons/1/19/Spotify_logo_without_text.svg"),
        TvAppItem("Apple TV+", "com.apple.atve.amazon.appletv", "", listOf(Color(0xFF333333), Color(0xFF111111))),
        TvAppItem("Settings", "com.android.tv.settings", "⚙", listOf(Color(0xFF64748B), Color(0xFF334155)))
    )

    val globalApps = listOf(
        TvAppItem("YouTube", "com.google.android.youtube.tv", "▶", listOf(Color(0xFFFF0000), Color(0xFF990000))),
        TvAppItem("Netflix", "com.netflix.ninja", "N", listOf(Color(0xFFE50914), Color(0xFF830A0F))),
        TvAppItem("Prime Video", "com.amazon.amazonvideo.livingroom", "P", listOf(Color(0xFF00A8E1), Color(0xFF005B7F))),
        TvAppItem("Disney+ Hotstar", "in.startv.hotstar", "★", listOf(Color(0xFF113CCF), Color(0xFF0B1B6D))),
        TvAppItem("Spotify", "com.spotify.tv.android", "♫", listOf(Color(0xFF1DB954), Color(0xFF0F5D2A))),
        TvAppItem("Apple TV+", "com.apple.atve.amazon.appletv", "", listOf(Color(0xFF333333), Color(0xFF111111))),
        TvAppItem("Hulu", "com.hulu.livingroomplus", "H", listOf(Color(0xFF1CE783), Color(0xFF0D6B3C))),
        TvAppItem("HBO Max / Max", "com.wbd.stream", "M", listOf(Color(0xFF002BE7), Color(0xFF001680))),
        TvAppItem("Twitch", "tv.twitch.android.app", "👾", listOf(Color(0xFF9146FF), Color(0xFF521B99))),
        TvAppItem("Settings", "com.android.tv.settings", "⚙", listOf(Color(0xFF64748B), Color(0xFF334155)))
    )

    val tvApps = if (isIndianRegion) indianApps else globalApps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "One-Tap TV App Launcher",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AccentCyan,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tvApps) { app ->
                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.90f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                    label = "AppScale"
                )

                // Try to load real app icon installed locally or on TV
                val localAppIconDrawable = remember(app.packageName) {
                    try {
                        context.packageManager.getApplicationIcon(app.packageName)
                    } catch (e: Exception) {
                        null
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                        .shadow(if (isPressed) 2.dp else 8.dp, RoundedCornerShape(20.dp))
                        .clickable {
                            isPressed = true
                            onLaunchApp(app.packageName)
                        },
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    LaunchedEffect(isPressed) { if (isPressed) { kotlinx.coroutines.delay(100); isPressed = false } }
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Brand Logo Icon Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(app.brandGradient))
                                .shadow(6.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (localAppIconDrawable != null) {
                                val imageBitmap = remember(localAppIconDrawable) {
                                    val bmp = android.graphics.Bitmap.createBitmap(
                                        localAppIconDrawable.intrinsicWidth.coerceAtLeast(1),
                                        localAppIconDrawable.intrinsicHeight.coerceAtLeast(1),
                                        android.graphics.Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = android.graphics.Canvas(bmp)
                                    localAppIconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                                    localAppIconDrawable.draw(canvas)
                                    bmp.asImageBitmap()
                                }
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = app.name,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = app.iconSymbol,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = app.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textCol,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (localAppIconDrawable != null) "Installed • Launch" else "Launch App",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (localAppIconDrawable != null) AccentEmerald else AccentCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreenOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF050811))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E1E))
                    .border(1.5.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "UnifiedCast",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "Universal Smart TV Remote",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentCyan,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = AccentCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManualModalDialog(
    onGetStarted: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onGetStarted,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📖", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "UnifiedCast User Guide",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Quick guide to pair & control your Smart TV",
                        fontSize = 11.sp,
                        color = AccentCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            ManualStepItem(
                stepNum = "1",
                title = "Connect to Same Wi-Fi Network",
                desc = "Ensure your smartphone and Android TV / Fire TV stick are connected to the same Wi-Fi router."
            )

            ManualStepItem(
                stepNum = "2",
                title = "Enable ADB / Wireless Remote",
                desc = "On Fire TV or Android TV, go to Settings ➔ My Fire TV ➔ Developer Options ➔ Enable ADB Debugging."
            )

            ManualStepItem(
                stepNum = "3",
                title = "Tap PAIR DEVICE",
                desc = "Tap the PAIR DEVICE pill in the top header. The app auto-discovers TV IP addresses on your subnet."
            )

            ManualStepItem(
                stepNum = "4",
                title = "Swipe & Control",
                desc = "Swipe left/right anywhere on screen to toggle between D-Pad, Volume/Media, Keyboard typing, and 1-Tap App Launchers."
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text(
                    text = "GET STARTED",
                    color = DarkBackground,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ManualStepItem(
    stepNum: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PairButtonGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(stepNum, color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

@Composable
fun FireTvRemoteSettingsScreen(
    useSwipeControl: Boolean,
    isVibrationEnabled: Boolean,
    onSwipeControlChanged: (Boolean) -> Unit,
    onVibrationChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

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

        // Swipe Control Option Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSwipeControlChanged(true) }
                .padding(start = 38.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Swipe Control",
                fontSize = 15.sp,
                color = Color.White
            )
            RadioButton(
                selected = useSwipeControl,
                onClick = { onSwipeControlChanged(true) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = FireTvBlue,
                    unselectedColor = Color(0xFF555555)
                )
            )
        }

        // D-Pad Control Option Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSwipeControlChanged(false) }
                .padding(start = 38.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "D-Pad Control",
                fontSize = 15.sp,
                color = Color.White
            )
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
        Divider(color = Color(0xFF262626), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Vibration Row
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
                Text(
                    text = "Vibration",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
        Divider(color = Color(0xFF262626), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // About & Version Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
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
                    Text("v1.0.0 (Build 1)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
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
                    Text(android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}
