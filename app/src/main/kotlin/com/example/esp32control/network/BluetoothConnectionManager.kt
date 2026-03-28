package com.example.esp32control.network

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Manages Bluetooth Classic connection with ESP32
 * Handles device discovery, pairing, and serial communication
 */
class BluetoothConnectionManager(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false
    
    companion object {
        private const val TAG = "BluetoothManager"
        // Standard UUID for Bluetooth Serial Port Profile
        private const val SERIAL_PORT_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        const val DEVICE_NAME = "LED_Control"
    }
    
    /**
     * Check if Bluetooth is available on device
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Get list of paired Bluetooth devices
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting paired devices: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Find LED_Control device in paired devices
     */
    fun findLEDControlDevice(): BluetoothDevice? {
        return getPairedDevices().find { it.name == DEVICE_NAME }
    }
    
    /**
     * Connect to a specific Bluetooth device
     */
    suspend fun connectToDevice(device: BluetoothDevice): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Cancel any existing discovery
                bluetoothAdapter?.cancelDiscovery()
                
                // Create socket
                bluetoothSocket = device.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERIAL_PORT_UUID)
                )
                
                // Connect
                bluetoothSocket?.connect()
                
                // Get streams
                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream
                
                isConnected = true
                Log.d(TAG, "Connected to ${device.name}")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed: ${e.message}")
                isConnected = false
                false
            }
        }
    }
    
    /**
     * Connect to LED_Control device (convenience method)
     */
    suspend fun connectToLEDControl(): Boolean {
        val device = findLEDControlDevice()
        return if (device != null) {
            connectToDevice(device)
        } else {
            Log.e(TAG, "LED_Control device not found in paired devices")
            false
        }
    }
    
    /**
     * Send command to ESP32
     */
    suspend fun sendCommand(command: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!isConnected || outputStream == null) {
                    Log.e(TAG, "Not connected to device")
                    return@withContext false
                }
                
                // Send command with newline terminator
                val commandWithNewline = "$command\n"
                outputStream?.write(commandWithNewline.toByteArray())
                outputStream?.flush()
                
                Log.d(TAG, "Command sent: $command")
                true
            } catch (e: IOException) {
                Log.e(TAG, "Error sending command: ${e.message}")
                isConnected = false
                false
            }
        }
    }
    
    /**
     * Receive response from ESP32
     */
    suspend fun receiveResponse(): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (!isConnected || inputStream == null) {
                    return@withContext null
                }
                
                val buffer = ByteArray(1024)
                val bytes = inputStream?.read(buffer)
                
                return@withContext if (bytes != null && bytes > 0) {
                    String(buffer, 0, bytes).trim()
                } else {
                    null
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error receiving response: ${e.message}")
                isConnected = false
                null
            }
        }
    }
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return isConnected && bluetoothSocket?.isConnected == true
    }
    
    /**
     * Disconnect from device
     */
    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
            isConnected = false
            Log.d(TAG, "Disconnected from device")
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting: ${e.message}")
        }
    }
}
