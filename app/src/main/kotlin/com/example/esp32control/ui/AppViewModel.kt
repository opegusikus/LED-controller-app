package com.example.esp32control.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esp32control.MyApp
import com.example.esp32control.data.DeviceRepository
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: DeviceRepository = getApplication<MyApp>().deviceRepository

    val connectionState = repo.connectionState
    val statusMessage = repo.statusMessage

    fun connect() { viewModelScope.launch { repo.connect() } }

    fun disconnect() { repo.disconnect() }

    fun sendCommand(command: String, value: Any) {
        viewModelScope.launch { repo.sendCommand(command, value) }
    }
}
