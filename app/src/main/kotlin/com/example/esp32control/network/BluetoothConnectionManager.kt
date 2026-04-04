package com.example.esp32control.network

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothConnectionManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false

    private val prefs = context.getSharedPreferences("bt_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "BluetoothManager"
        private const val SERIAL_PORT_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        private const val PREF_DEVICE_MAC = "saved_device_mac"
        const val DEVICE_NAME = "LED_Control"
    }

    fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun getSavedMac(): String? = prefs.getString(PREF_DEVICE_MAC, null)

    private fun saveDeviceMac(mac: String) {
        prefs.edit().putString(PREF_DEVICE_MAC, mac).apply()
        Log.d(TAG, "Saved device MAC: $mac")
    }

    fun clearSavedMac() {
        prefs.edit().remove(PREF_DEVICE_MAC).apply()
        Log.d(TAG, "Cleared saved MAC")
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting paired devices: ${e.message}")
            emptyList()
        }
    }

    fun findLEDControlDevice(): BluetoothDevice? {
        return getPairedDevices().find { it.name == DEVICE_NAME }
    }

    /**
     * Connect by saved MAC first (fast path), falls back to name search if not found.
     * Returns the connected device name, or null on failure.
     */
    suspend fun autoConnect(): String? {
        val savedMac = getSavedMac()

        if (savedMac != null) {
            Log.d(TAG, "Trying saved MAC: $savedMac")
            val device = getPairedDevices().find { it.address == savedMac }
            if (device != null) {
                return if (connectToDevice(device)) device.name else null
            }
            Log.w(TAG, "Saved MAC not in paired devices, falling back to name search")
        }

        // Fall back to name-based search
        val device = findLEDControlDevice()
        return if (device != null && connectToDevice(device)) device.name else null
    }

    suspend fun connectToDevice(device: BluetoothDevice): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                bluetoothAdapter?.cancelDiscovery()

                Log.d(TAG, "Connecting to ${device.name} (${device.address})")

                bluetoothSocket = try {
                    device.createRfcommSocketToServiceRecord(UUID.fromString(SERIAL_PORT_UUID))
                } catch (e: Exception) {
                    Log.w(TAG, "Secure socket failed, trying insecure: ${e.message}")
                    device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SERIAL_PORT_UUID))
                }

                bluetoothSocket?.connect()

                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream

                if (inputStream == null || outputStream == null) {
                    Log.e(TAG, "Failed to get streams")
                    isConnected = false
                    return@withContext false
                }

                isConnected = true
                saveDeviceMac(device.address)
                Log.d(TAG, "Connected to ${device.name}, MAC saved")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed: ${e.message}")
                isConnected = false
                bluetoothSocket = null
                false
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed: ${e.message}")
                isConnected = false
                bluetoothSocket = null
                false
            }
        }
    }

    suspend fun sendCommand(command: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!isConnected || outputStream == null) {
                    Log.e(TAG, "Not connected")
                    return@withContext false
                }
                outputStream?.write("$command\n".toByteArray())
                outputStream?.flush()
                Log.d(TAG, "Sent: $command")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Send failed: ${e.message}")
                isConnected = false
                false
            }
        }
    }

    fun isConnected(): Boolean = isConnected && bluetoothSocket?.isConnected == true

    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
            isConnected = false
            Log.d(TAG, "Disconnected")
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting: ${e.message}")
        }
    }
}
