package com.codesrahul.unifiedcast.network

import android.util.Log
import okhttp3.*
import org.json.JSONObject

class WebSocketClient(
    private val onMessageReceived: (JSONObject) -> Unit,
    private val onConnectionStateChanged: (Boolean, String) -> Unit
) {
    private val client = OkHttpClient.Builder().build()
    private var webSocket: WebSocket? = null

    fun connect(ip: String, port: Int = 9090) {
        val url = "ws://$ip:$port"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("UnifiedCastWS", "Connected to $url")
                onConnectionStateChanged(true, "Connected to $ip:$port")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    onMessageReceived(json)
                } catch (e: Exception) {
                    Log.e("UnifiedCastWS", "Error parsing json", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("UnifiedCastWS", "Connection failed", t)
                onConnectionStateChanged(false, "Disconnected / Failed: ${t.localizedMessage}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onConnectionStateChanged(false, "Disconnected")
            }
        })
    }

    fun send(type: String, payload: JSONObject = JSONObject()) {
        payload.put("type", type)
        webSocket?.send(payload.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }
}
