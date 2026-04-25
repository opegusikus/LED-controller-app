package com.example.esp32control.ui.bluetooth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esp32control.MyApp
import kotlinx.coroutines.launch

class BluetoothViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = getApplication<MyApp>().deviceRepository

    val connectionState = repo.connectionState
    val statusMessage = repo.statusMessage

    fun connect() { viewModelScope.launch { repo.connect() } }

    fun disconnect() { repo.disconnect() }
}
