package com.example.esp32control.ui.modes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esp32control.MyApp
import kotlinx.coroutines.launch

class ModesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = getApplication<MyApp>().deviceRepository

    fun sendCommand(command: String, value: Any) {
        viewModelScope.launch { repo.sendCommand(command, value) }
    }
}
