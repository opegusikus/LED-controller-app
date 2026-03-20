package com.example.esp32control.models

import com.google.gson.annotations.SerializedName

/**
 * Data class for ESP32 command requests
 */
data class Command(
    @SerializedName("command")
    val command: String,
    
    @SerializedName("value")
    val value: Any? = null
)

/**
 * Response from ESP32 API
 */
data class ApiResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: Map<String, Any>? = null
)

/**
 * Connection state and device info
 */
data class DeviceInfo(
    @SerializedName("device_name")
    val deviceName: String = "ESP32",
    
    @SerializedName("firmware_version")
    val firmwareVersion: String? = null,
    
    @SerializedName("ip_address")
    val ipAddress: String? = null
)

/**
 * Current lighting state
 */
data class LightingState(
    @SerializedName("mode")
    val mode: String = "static_light", // static_light, rainbow, pulse, etc.
    
    @SerializedName("brightness")
    val brightness: Int = 128, // 0-255
    
    @SerializedName("rgb")
    val rgb: RGBColor? = null,
    
    @SerializedName("is_on")
    val isOn: Boolean = true
)

/**
 * RGB color representation
 */
data class RGBColor(
    @SerializedName("r")
    val red: Int = 0,
    
    @SerializedName("g")
    val green: Int = 0,
    
    @SerializedName("b")
    val blue: Int = 0
)
