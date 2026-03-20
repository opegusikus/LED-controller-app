package com.example.esp32control.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ESP32 API Client using Retrofit and OkHttp
 * Handles all HTTP communication with the ESP32 device
 */
object ESP32ApiClient {
    
    private var retrofit: Retrofit? = null
    private var apiService: ESP32Api? = null
    private var baseUrl: String = "http://192.168.1.100:80"
    
    /**
     * Setup the API client with configuration
     * @param deviceIp The IP address of the ESP32 device
     * @param port The port number (default 80)
     * @param debugMode Enable HTTP logging for debugging
     */
    fun setup(
        deviceIp: String,
        port: Int = 80,
        debugMode: Boolean = true
    ) {
        baseUrl = "http://$deviceIp:$port"
        buildRetrofitClient(debugMode)
    }
    
    /**
     * Build Retrofit client with OkHttp configuration
     */
    private fun buildRetrofitClient(debugMode: Boolean) {
        val httLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (debugMode) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httLoggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        
        val gson = GsonBuilder()
            .setLenient()
            .create()
        
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        apiService = retrofit!!.create(ESP32Api::class.java)
    }
    
    /**
     * Get the API service instance
     * Use this to make API calls
     */
    fun getService(): ESP32Api {
        if (apiService == null) {
            buildRetrofitClient(debugMode = false)
        }
        return apiService ?: throw IllegalStateException("API Service not initialized")
    }
    
    /**
     * Update base URL if device IP changes
     */
    fun updateDeviceIp(newIp: String, port: Int = 80) {
        baseUrl = "http://$newIp:$port"
        buildRetrofitClient(debugMode = false)
    }
    
    /**
     * Get current base URL
     */
    fun getBaseUrl(): String = baseUrl
}
