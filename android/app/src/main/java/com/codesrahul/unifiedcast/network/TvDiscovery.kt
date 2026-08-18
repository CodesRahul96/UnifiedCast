package com.codesrahul.unifiedcast.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class TvDiscovery(context: Context, private val onTvDiscovered: (String, Int, String) -> Unit) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    // Standard Android TV & Fire TV Zeroconf / mDNS service signatures
    private val serviceTypes = listOf(
        "_androidtvremote2._tcp.",
        "_adb._tcp.",
        "_googlecast._tcp."
    )

    fun startDiscovery() {
        serviceTypes.forEach { type ->
            try {
                nsdManager.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, object : NsdManager.DiscoveryListener {
                    override fun onDiscoveryStarted(regType: String) {
                        Log.d("TvDiscovery", "Started scanning for $regType")
                    }

                    override fun onServiceFound(service: NsdServiceInfo) {
                        nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                val host = serviceInfo.host?.hostAddress ?: return
                                val port = if (serviceInfo.port > 0) serviceInfo.port else 5555
                                val rawName = serviceInfo.serviceName ?: "Android Smart TV"
                                val cleanName = parseFriendlyNsdName(rawName, host)
                                Log.d("TvDiscovery", "Discovered TV: $cleanName at $host:$port")
                                onTvDiscovered(host, port, cleanName)
                            }
                        })
                    }

                    override fun onServiceLost(service: NsdServiceInfo) {}
                    override fun onDiscoveryStopped(serviceType: String) {}
                    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
                    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
                })
            } catch (e: Exception) {
                Log.e("TvDiscovery", "Error scanning $type", e)
            }
        }
    }

    private fun parseFriendlyNsdName(raw: String, ip: String): String {
        val name = raw.replace("-", " ").replace("_", " ").trim()
        return when {
            name.contains("fire", ignoreCase = true) || name.contains("amazon", ignoreCase = true) -> "Amazon Fire TV"
            name.contains("bravia", ignoreCase = true) || name.contains("sony", ignoreCase = true) -> "Sony Bravia Android TV"
            name.contains("shield", ignoreCase = true) || name.contains("nvidia", ignoreCase = true) -> "Nvidia Shield TV"
            name.contains("chromecast", ignoreCase = true) || name.contains("google", ignoreCase = true) -> "Google TV / Chromecast"
            name.contains("mi", ignoreCase = true) || name.contains("xiaomi", ignoreCase = true) -> "Xiaomi Mi TV"
            name.contains("tcl", ignoreCase = true) -> "TCL Smart TV"
            name.contains("hisense", ignoreCase = true) -> "Hisense Smart TV"
            name.length in 3..24 && !name.contains("android", ignoreCase = true) -> name
            else -> "Smart TV ($ip)"
        }
    }
}
