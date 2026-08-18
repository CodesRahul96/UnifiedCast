package com.codesrahul.unifiedcast.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

data class DiscoveredDevice(
    val name: String,
    val ip: String,
    val port: Int = 5555,
    val deviceType: String = "Android TV / Fire TV",
    val brandIcon: String = "TV"
)

class SubnetScanner(private val context: Context) {

    suspend fun scanLocalWifiSubnet(
        onDeviceFound: (DiscoveredDevice) -> Unit
    ): List<DiscoveredDevice> = withContext(Dispatchers.IO) {
        val discoveredList = mutableListOf<DiscoveredDevice>()
        val localIp = getLocalWifiIpAddress() ?: return@withContext emptyList()
        val subnetPrefix = localIp.substringBeforeLast(".")

        Log.d("SubnetScanner", "Fast scanning subnet: $subnetPrefix.1 - 254 for ports 5555, 6466, 8080, 7000, 8008")

        // Read ARP table first to get real device hostnames & MAC vendor info
        val arpMap = getArpTableDevices()

        // Asynchronously check hosts in parallel with low timeout for responsive discovery
        val jobs = (1..254).map { i ->
            async(Dispatchers.IO) {
                val host = "$subnetPrefix.$i"
                checkHostForTv(host, arpMap[host], onDeviceFound, discoveredList)
            }
        }
        jobs.awaitAll()

        discoveredList
    }

    private fun checkHostForTv(
        host: String,
        arpName: String?,
        onDeviceFound: (DiscoveredDevice) -> Unit,
        discoveredList: MutableList<DiscoveredDevice>
    ) {
        val portsToCheck = listOf(5555, 6466, 8080, 7000, 8008, 1900)
        for (port in portsToCheck) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), 250)
                socket.close()

                val canonicalHost = try {
                    val inet = InetAddress.getByName(host)
                    inet.canonicalHostName
                } catch (e: Exception) { host }

                val resolvedName = when {
                    arpName != null && !arpName.contains("Smart TV Device") -> arpName
                    canonicalHost != host && !canonicalHost.contains("ip-") && !canonicalHost.contains("192.168") -> canonicalHost.substringBefore(".")
                    else -> getFriendlyDeviceName(host, port)
                }

                val brand = when {
                    resolvedName.contains("fire", ignoreCase = true) || resolvedName.contains("amazon", ignoreCase = true) -> "Amazon Fire TV"
                    resolvedName.contains("bravia", ignoreCase = true) || resolvedName.contains("sony", ignoreCase = true) -> "Sony Bravia Android TV"
                    resolvedName.contains("shield", ignoreCase = true) || resolvedName.contains("nvidia", ignoreCase = true) -> "Nvidia Shield TV"
                    resolvedName.contains("tcl", ignoreCase = true) -> "TCL Google TV"
                    resolvedName.contains("hisense", ignoreCase = true) -> "Hisense Smart TV"
                    resolvedName.contains("mi", ignoreCase = true) || resolvedName.contains("xiaomi", ignoreCase = true) -> "Xiaomi Mi TV Box"
                    resolvedName.contains("samsung", ignoreCase = true) -> "Samsung Smart TV"
                    resolvedName.contains("lg", ignoreCase = true) -> "LG webOS TV"
                    resolvedName.contains("desktop", ignoreCase = true) -> "PC Remote Daemon ($host)"
                    else -> "Android TV ($host)"
                }

                val devType = when (port) {
                    5555 -> "ADB Wireless Remote"
                    6466 -> "Android Remote Service v2"
                    8008, 7000 -> "Chromecast / DIAL Remote"
                    else -> "Smart Remote Protocol"
                }

                val dev = DiscoveredDevice(
                    name = brand,
                    ip = host,
                    port = port,
                    deviceType = devType,
                    brandIcon = "TV"
                )

                synchronized(discoveredList) {
                    if (discoveredList.none { it.ip == host }) {
                        discoveredList.add(dev)
                        onDeviceFound(dev)
                    }
                }
                break
            } catch (e: Exception) {
                // Closed port or unreachable host
            }
        }
    }

    private fun getFriendlyDeviceName(ip: String, port: Int): String {
        return when (ip.substringAfterLast(".")) {
            "246" -> "Linux TV Receiver"
            else -> "Smart TV ($ip)"
        }
    }

    private fun getArpTableDevices(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val proc = Runtime.getRuntime().exec("cat /proc/net/arp")
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split("\\s+".toRegex())
                if (tokens.size >= 6 && tokens[0] != "IP") {
                    val ip = tokens[0]
                    val mac = tokens[3]
                    if (mac != "00:00:00:00:00:00") {
                        val vendor = identifyMacVendor(mac)
                        map[ip] = "$vendor ($ip)"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SubnetScanner", "Error reading ARP table", e)
        }
        return map
    }

    private fun identifyMacVendor(mac: String): String {
        val prefix = mac.uppercase().take(8)
        return when {
            prefix.startsWith("FC:A1:83") || prefix.startsWith("00:BB:3A") || prefix.startsWith("50:DC:E7") || prefix.startsWith("B4:7C:9C") -> "Amazon Fire TV"
            prefix.startsWith("00:04:1F") || prefix.startsWith("00:24:BE") || prefix.startsWith("70:26:05") -> "Sony Bravia TV"
            prefix.startsWith("00:04:4B") || prefix.startsWith("B8:27:EB") -> "Nvidia Shield"
            prefix.startsWith("D4:9E:3B") || prefix.startsWith("64:CE:64") || prefix.startsWith("70:66:1B") -> "Xiaomi Mi Box"
            prefix.startsWith("AC:12:03") || prefix.startsWith("70:AF:6A") -> "TCL Google TV"
            prefix.startsWith("48:44:F7") || prefix.startsWith("6C:E8:5C") -> "Samsung Smart TV"
            prefix.startsWith("10:E5:6A") || prefix.startsWith("3C:CD:36") -> "LG Smart TV"
            else -> "Smart TV Device"
        }
    }

    private fun getLocalWifiIpAddress(): String? {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return null
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return null
            if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SubnetScanner", "Error getting wifi IP", e)
        }
        return null
    }
}
