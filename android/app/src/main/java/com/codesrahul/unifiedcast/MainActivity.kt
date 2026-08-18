package com.codesrahul.unifiedcast

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.codesrahul.unifiedcast.hardware.IrRemoteManager
import com.codesrahul.unifiedcast.network.DiscoveredDevice
import com.codesrahul.unifiedcast.network.SubnetScanner
import com.codesrahul.unifiedcast.network.TvDiscovery
import com.codesrahul.unifiedcast.network.TvRemoteClient
import com.codesrahul.unifiedcast.ui.components.*
import com.codesrahul.unifiedcast.ui.theme.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private external fun stringFromJNI(): String

    companion object {
        init {
            System.loadLibrary("unifiedcast")
        }
    }

    private fun getNativeEngineVersion(): String {
        return try {
            stringFromJNI()
        } catch (e: UnsatisfiedLinkError) {
            "v1.0.0-cpp"
        }
    }

    private val tvClient = TvRemoteClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val irManager = IrRemoteManager(this)

        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val prefs = getSharedPreferences("unified_cast_prefs", Context.MODE_PRIVATE)

        setContent {
            MaterialTheme(colorScheme = DarkColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    var showSplash by remember { mutableStateOf(true) }
                    var showUserManual by remember { mutableStateOf(prefs.getBoolean("is_first_time", true)) }

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

                    // Firebase Remote Config Initialization
                    var remoteAppVersion by remember { mutableStateOf("v1.0.0") }
                    val remoteConfig = remember { FirebaseRemoteConfig.getInstance() }

                    LaunchedEffect(Unit) {
                        try {
                            val configSettings = FirebaseRemoteConfigSettings.Builder()
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
                        if (!savedIp.isNullOrBlank()) {
                            tvIpAddress = savedIp
                            val savedName = prefs.getString("last_tv_name", "Smart TV") ?: "Smart TV"
                            activeDeviceName = savedName
                            connectionStatus = "Connecting to $savedName ($savedIp)..."
                            val success = tvClient.connect(savedIp, 5555)
                            isConnected = success
                            connectionStatus = if (success) "Connected to $savedName" else "Saved TV ($savedIp) Offline"
                        }
                    }

                    fun startSubnetPairScan() {
                        isScanning = true
                        discoveredDevices.clear()

                        val tvDiscovery = TvDiscovery(this@MainActivity) { ip, port, name ->
                            val dev = DiscoveredDevice(name = name, ip = ip, port = port)
                            if (discoveredDevices.none { it.ip == dev.ip }) {
                                discoveredDevices.add(dev)
                            }
                        }
                        tvDiscovery.startDiscovery()

                        val subnetScanner = SubnetScanner(this@MainActivity)
                        coroutineScope.launch {
                            subnetScanner.scanLocalWifiSubnet { device ->
                                if (discoveredDevices.none { it.ip == device.ip }) {
                                    discoveredDevices.add(device)
                                }
                            }
                            isScanning = false
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        TopHeader(
                            tvIpAddress = tvIpAddress,
                            activeDeviceName = activeDeviceName,
                            isConnected = isConnected,
                            onPairClick = {
                                showPairDialog = true
                                startSubnetPairScan()
                            }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceDark.copy(alpha = 0.85f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
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
                            val tabOrder = remember { listOf(2, 1, 0, 3, 4) }
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    val targetIndex = tabOrder.indexOf(targetState)
                                    val initialIndex = tabOrder.indexOf(initialState)
                                    if (targetIndex > initialIndex) {
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
                                            irManager.transmitKey(code)
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
                                        },
                                        onShowGuide = { showUserManual = true }
                                    )
                                }
                            }
                        }

                        FloatingBottomNavBar(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }

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

                    if (showUserManual) {
                        UserManualModalDialog(
                            onGetStarted = {
                                prefs.edit().putBoolean("is_first_time", false).apply()
                                showUserManual = false
                            }
                        )
                    }

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

val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentEmerald,
    background = DarkBackground,
    surface = SurfaceDark
)
