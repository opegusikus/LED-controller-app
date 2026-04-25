package com.example.esp32control

import android.app.Application
import com.example.esp32control.data.DeviceRepository
import com.example.esp32control.data.DeviceRepositoryImpl
import com.example.esp32control.network.BluetoothConnectionManager

class MyApp : Application() {
    val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(BluetoothConnectionManager(this))
    }
}
