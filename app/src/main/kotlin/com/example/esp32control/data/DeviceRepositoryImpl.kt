package com.example.esp32control.data

import android.util.Log
import com.example.esp32control.network.BluetoothConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class DeviceRepositoryImpl(
    private val btManager: BluetoothConnectionManager
) : DeviceRepository {

    companion object {
        private const val TAG = "DeviceRepository"
    }

    private val _connectionState = MutableStateFlow(ConnectionState.Idle)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    override val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        if (btManager.isConnected()) _connectionState.value = ConnectionState.Connected
    }

    override suspend fun connect() {
        _connectionState.value = ConnectionState.Connecting
        val deviceName = btManager.autoConnect()
        if (deviceName != null) {
            _connectionState.value = ConnectionState.Connected
            _statusMessage.value = ""
        } else {
            _connectionState.value = ConnectionState.Failed
            _statusMessage.value = if (btManager.getSavedMac() != null) {
                "Device not in range"
            } else {
                "Not paired — use Scan to find device"
            }
        }
    }

    override fun disconnect() {
        btManager.disconnect()
        _connectionState.value = ConnectionState.Idle
        _statusMessage.value = ""
    }

    override suspend fun sendCommand(command: String, value: Any) {
        if (!btManager.isConnected()) {
            _statusMessage.value = "Not connected"
            Log.w(TAG, "sendCommand called while not connected")
            return
        }
        val json = JSONObject().apply {
            put("command", command)
            put("value", value)
        }
        val success = btManager.sendCommand(json.toString())
        if (!success) {
            _statusMessage.value = "Send failed"
        }
    }

    override fun hasSavedDevice(): Boolean = btManager.getSavedMac() != null
}
