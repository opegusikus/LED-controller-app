package com.example.esp32control.network

import com.example.esp32control.models.ApiResponse
import com.example.esp32control.models.Command
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API interface for ESP32 communication
 * Endpoints are relative to the base URL defined in ESP32ApiClient
 */
interface ESP32Api {
    
    @POST("api/command")
    suspend fun sendCommand(@Body command: Command): ApiResponse
    
    @GET("api/state")
    suspend fun getCurrentState(): ApiResponse
    
    @GET("api/info")
    suspend fun getDeviceInfo(): ApiResponse
}
