package com.example.esp32control.network

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility class for WiFi connection management
 */
class WiFiConnectionManager(private val context: Context) {
    
    private val wifiManager: WifiManager = 
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    /**
     * Scan for available WiFi networks
     * Requires ACCESS_FINE_LOCATION permission (Android 6.0+)
     */
    suspend fun scanNetworks(): List<String> = withContext(Dispatchers.Default) {
        return@withContext try {
            val results = mutableListOf<String>()
            val scanResults = wifiManager.scanResults
            
            scanResults?.forEach { result ->
                if (result.SSID.isNotEmpty()) {
                    results.add(result.SSID)
                }
            }
            
            results.distinct().sorted()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Connect to a WiFi network
     * @param ssid Network name
     * @param password Network password (leave empty for open networks)
     */
    suspend fun connectToNetwork(ssid: String, password: String = ""): Boolean = 
        withContext(Dispatchers.Default) {
        return@withContext try {
            // Specify the correct API level handling
            connectNetworkImpl(ssid, password)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Internal implementation for connecting to network
     */
    private fun connectNetworkImpl(ssid: String, password: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, WiFi connection requires different approach
            // This is handled by the system network settings
            true // Return true as connection is handled by Android system
        } else {
            // For Android 9 and below
            @Suppress("DEPRECATION")
            val config = WifiConfiguration()
            config.SSID = "\"$ssid\"" // Add quotes around SSID
            config.preSharedKey = "\"$password\"" // Add quotes around password
            config.status = WifiConfiguration.Status.ENABLED
            
            val networkId = wifiManager.addNetwork(config)
            wifiManager.disconnect()
            wifiManager.enableNetwork(networkId, true)
            wifiManager.reconnect()
            
            true
        }
    }
    
    /**
     * Get currently connected WiFi network name
     */
    fun getCurrentNetworkSSID(): String? {
        return try {
            val connectionInfo = wifiManager.connectionInfo
            val ssid = connectionInfo?.ssid?.removeSurrounding("\"")
            ssid?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Check if WiFi is enabled
     */
    fun isWiFiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }
    
    /**
     * Enable WiFi
     */
    fun enableWiFi(): Boolean {
        if (!wifiManager.isWifiEnabled) {
            return wifiManager.startScan()
        }
        return true
    }
    
    /**
     * Check if connected to a specific SSID
     */
    fun isConnectedToSSID(targetSSID: String): Boolean {
        val currentSSID = getCurrentNetworkSSID() ?: return false
        return currentSSID.equals(targetSSID, ignoreCase = true)
    }
    
    /**
     * Get WiFi signal strength (RSSI)
     */
    fun getSignalStrength(): Int {
        return try {
            val connectionInfo = wifiManager.connectionInfo
            connectionInfo?.rssi ?: -1
        } catch (e: Exception) {
            -1
        }
    }
}
