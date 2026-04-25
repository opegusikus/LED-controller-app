package com.example.esp32control.data

import kotlinx.coroutines.flow.StateFlow

enum class ConnectionState { Idle, Connecting, Connected, Failed }

interface DeviceRepository {
    val connectionState: StateFlow<ConnectionState>
    val statusMessage: StateFlow<String>
    suspend fun connect()
    fun disconnect()
    suspend fun sendCommand(command: String, value: Any)
    fun hasSavedDevice(): Boolean
}
