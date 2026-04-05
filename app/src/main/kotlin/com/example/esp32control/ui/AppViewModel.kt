package com.example.esp32control.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esp32control.network.BluetoothConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class ConnectionState { Idle, Connecting, Connected, Failed }

class AppViewModel(app: Application) : AndroidViewModel(app) {

    val bluetoothManager = BluetoothConnectionManager(app)

    private val _connectionState = MutableStateFlow(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        if (bluetoothManager.isConnected()) {
            _connectionState.value = ConnectionState.Connected
        }
    }

    fun connect() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Connecting
            val deviceName = bluetoothManager.autoConnect()
            if (deviceName != null) {
                _connectionState.value = ConnectionState.Connected
                _statusMessage.value = ""
            } else {
                _connectionState.value = ConnectionState.Failed
                val hasSavedMac = bluetoothManager.getSavedMac() != null
                _statusMessage.value = if (hasSavedMac) "Device not in range" else "Not paired — use Scan to find device"
            }
        }
    }

    fun disconnect() {
        bluetoothManager.disconnect()
        _connectionState.value = ConnectionState.Idle
        _statusMessage.value = ""
    }

    fun sendCommand(command: String, value: Any) {
        viewModelScope.launch {
            if (!bluetoothManager.isConnected()) {
                _statusMessage.value = "Not connected"
                return@launch
            }
            val json = JSONObject().apply {
                put("command", command)
                put("value", value)
            }
            bluetoothManager.sendCommand(json.toString())
        }
    }
}
