package com.codesrahul.unifiedcast.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Direct Wireless TV Remote Client using standard Android ADB / KeyEvent sockets over TCP 5555
 * Compatible out of the box with Android TV, Google TV, Fire TV, Nvidia Shield, and Sony/TCL/Hisense Smart TVs.
 */
class TvRemoteClient {

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var isConnected = false

    suspend fun connect(ipAddress: String, port: Int = 5555): Boolean = withContext(Dispatchers.IO) {
        try {
            disconnect()
            Log.d("TvRemoteClient", "Connecting directly to TV at $ipAddress:$port")
            val sock = Socket()
            sock.connect(InetSocketAddress(ipAddress, port), 3000)
            socket = sock
            outputStream = sock.getOutputStream()
            inputStream = sock.getInputStream()
            isConnected = true
            Log.d("TvRemoteClient", "Connected to TV successfully!")
            true
        } catch (e: Exception) {
            Log.e("TvRemoteClient", "Failed to connect to TV at $ipAddress:$port", e)
            isConnected = false
            false
        }
    }

    suspend fun sendKeyEvent(keyCode: Int): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected || socket == null) return@withContext false
        try {
            // Execute primary input keyevent
            val cmd = "input keyevent $keyCode\n"
            outputStream?.write(cmd.toByteArray())
            outputStream?.flush()

            // Transmit alternative fallback keycodes for universal device compatibility (e.g. Fire TV Stick / Sony / TCL)
            val fallbacks = when (keyCode) {
                TvKeyCodes.KEYCODE_POWER -> listOf(223, 224) // KEYCODE_SLEEP, KEYCODE_WAKEUP
                TvKeyCodes.KEYCODE_SETTINGS -> listOf(225, 176) // KEYCODE_PAIRING, KEYCODE_SETTINGS
                TvKeyCodes.KEYCODE_TV_INPUT -> listOf(178, 224) // KEYCODE_TV_INPUT, KEYCODE_WAKEUP
                TvKeyCodes.KEYCODE_VOLUME_MUTE -> listOf(164, 91) // KEYCODE_VOLUME_MUTE, KEYCODE_MUTE
                TvKeyCodes.KEYCODE_GUIDE -> listOf(172, 82) // KEYCODE_GUIDE, KEYCODE_MENU
                else -> emptyList()
            }

            for (fb in fallbacks) {
                kotlinx.coroutines.delay(40)
                outputStream?.write("input keyevent $fb\n".toByteArray())
                outputStream?.flush()
            }
            true
        } catch (e: Exception) {
            Log.e("TvRemoteClient", "Error sending keycode $keyCode", e)
            isConnected = false
            false
        }
    }

    suspend fun sendTextInput(text: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected || socket == null) return@withContext false
        try {
            val sanitized = text.replace(" ", "%s").replace("'", "\\'")
            val cmd = "input text $sanitized\n"
            outputStream?.write(cmd.toByteArray())
            outputStream?.flush()
            true
        } catch (e: Exception) {
            Log.e("TvRemoteClient", "Error sending text input", e)
            false
        }
    }

    suspend fun launchApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected || socket == null) return@withContext false
        try {
            val cmd = "monkey -p $packageName -c android.intent.category.LAUNCHER 1\n"
            outputStream?.write(cmd.toByteArray())
            outputStream?.flush()
            true
        } catch (e: Exception) {
            Log.e("TvRemoteClient", "Error launching app $packageName", e)
            false
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: Exception) {}
        socket = null
        outputStream = null
        inputStream = null
        isConnected = false
    }

    fun isConnected(): Boolean = isConnected
}

// Android KeyCode Constants for Remote Control
object TvKeyCodes {
    const val KEYCODE_DPAD_UP = 19
    const val KEYCODE_DPAD_DOWN = 20
    const val KEYCODE_DPAD_LEFT = 21
    const val KEYCODE_DPAD_RIGHT = 22
    const val KEYCODE_DPAD_CENTER = 23
    const val KEYCODE_BACK = 4
    const val KEYCODE_HOME = 3
    const val KEYCODE_MENU = 82
    const val KEYCODE_VOLUME_UP = 24
    const val KEYCODE_VOLUME_DOWN = 25
    const val KEYCODE_VOLUME_MUTE = 164
    const val KEYCODE_POWER = 26
    const val KEYCODE_MEDIA_PLAY_PAUSE = 85
    const val KEYCODE_MEDIA_NEXT = 87
    const val KEYCODE_MEDIA_PREVIOUS = 88
    const val KEYCODE_CHANNEL_UP = 166
    const val KEYCODE_CHANNEL_DOWN = 167
    const val KEYCODE_MEDIA_FAST_FORWARD = 90
    const val KEYCODE_MEDIA_REWIND = 89
    const val KEYCODE_TV_INPUT = 178
    const val KEYCODE_INFO = 165
    const val KEYCODE_GUIDE = 172
    const val KEYCODE_SETTINGS = 176
}
