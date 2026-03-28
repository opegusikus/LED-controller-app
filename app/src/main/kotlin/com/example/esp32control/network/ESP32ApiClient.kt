package com.example.esp32control.network

import com.google.gson.GsonBuilder
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * ESP32 API Client using Retrofit and OkHttp
 * Handles all HTTPS communication with the ESP32 device
 */
object ESP32ApiClient {
    
    private var retrofit: Retrofit? = null
    private var apiService: ESP32Api? = null
    private var baseUrl: String = "https://192.168.4.1:443/"
    private var currentDebugMode: Boolean = true
    
    /**
     * Setup the API client with configuration
     */
    fun setup(
        deviceIp: String,
        port: Int = 443,
        debugMode: Boolean = true
    ) {
        currentDebugMode = debugMode
        baseUrl = "https://$deviceIp:$port/"
        buildRetrofitClient()
    }
    
    private fun buildRetrofitClient() {
        val httLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (currentDebugMode) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        // Trust Manager for self-signed certificates
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        
        // Support both Modern and Compatible TLS for better compatibility with ESP32 SSL
        val connectionSpecs = listOf(
            ConnectionSpec.MODERN_TLS,
            ConnectionSpec.COMPATIBLE_TLS
        )
        
        val okHttpClient = OkHttpClient.Builder()
            .connectionSpecs(connectionSpecs)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(httLoggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
        
        apiService = retrofit!!.create(ESP32Api::class.java)
    }
    
    fun getService(): ESP32Api {
        if (apiService == null) buildRetrofitClient()
        return apiService!!
    }
    
    fun updateDeviceIp(newIp: String, port: Int = 443) {
        baseUrl = "https://$newIp:$port/"
        buildRetrofitClient()
    }
}
