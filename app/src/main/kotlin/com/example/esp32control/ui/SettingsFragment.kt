package com.example.esp32control.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.esp32control.databinding.FragmentSettingsBinding
import com.example.esp32control.models.Command
import com.example.esp32control.network.ESP32ApiClient
import com.example.esp32control.network.WiFiConnectionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fragment for device settings
 * Provides controls for brightness, WiFi connection, and other device settings
 */
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private var brightnessJob: Job? = null
    private var isUserDragging = false
    private lateinit var wifiManager: WiFiConnectionManager
    private var scanJob: Job? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiManager = WiFiConnectionManager(requireContext())
        setupControls()
        initializeValues()
        updateWiFiStatus()
    }
    
    private fun setupControls() {
        // Brightness slider setup
        binding.brightnessSlider.apply {
            min = 0
            max = 255
            progress = 128
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        binding.brightnessValue.text = "$progress"
                        isUserDragging = true
                        sendBrightnessUpdate(progress)
                    }
                }
                
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    brightnessJob?.cancel()
                }
                
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isUserDragging = false
                }
            })
        }
        
        // Device IP address button
        binding.btnUpdateIp.setOnClickListener {
            val ip = binding.etDeviceIp.text.toString().trim()
            if (ip.isNotEmpty()) {
                updateDeviceIp(ip)
            } else {
                showToast("Please enter a valid IP address")
            }
        }
        
        // Refresh button to get current device state
        binding.btnRefreshState.setOnClickListener {
            refreshDeviceState()
        }
        
        // WiFi network scan button
        binding.btnScanWifi.setOnClickListener {
            scanWiFiNetworks()
        }
        
        // Connect to LED_control button
        binding.btnConnectLedControl.setOnClickListener {
            connectToLEDControl()
        }
    }
    
    private fun initializeValues() {
        // Set initial values
        binding.brightnessValue.text = "128"
        binding.brightnessSlider.progress = 128
    }
    
    /**
     * Send brightness update with debouncing
     * Sends update after 300ms of user stopping to adjust slider
     */
    private fun sendBrightnessUpdate(brightness: Int) {
        brightnessJob?.cancel()
        brightnessJob = lifecycleScope.launch {
            delay(300) // Debounce delay
            
            try {
                val apiService = ESP32ApiClient.getService()
                val command = Command(command = "brightness", value = brightness)
                
                val response = apiService.sendCommand(command)
                showToast("Brightness set to $brightness")
                
            } catch (e: Exception) {
                showToast("Error: Failed to update brightness - ${e.message}")
            }
        }
    }
    
    /**
     * Update the device IP address
     */
    private fun updateDeviceIp(ip: String) {
        try {
            ESP32ApiClient.updateDeviceIp(ip, port = 80)
            showToast("Device IP updated to $ip")
            binding.etDeviceIp.clearFocus()
        } catch (e: Exception) {
            showToast("Error: Invalid IP address - ${e.message}")
        }
    }
    
    /**
     * Refresh current device state
     */
    private fun refreshDeviceState() {
        lifecycleScope.launch {
            try {
                val apiService = ESP32ApiClient.getService()
                val response = apiService.getCurrentState()
                
                if (response.status == "success") {
                    showToast("Device state: ${response.data?.get("mode")}")
                } else {
                    showToast("Failed to get device state")
                }
                
            } catch (e: Exception) {
                showToast("Error: Connection failed - ${e.message}")
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Update WiFi status display
     */
    private fun updateWiFiStatus() {
        val currentSSID = wifiManager.getCurrentNetworkSSID() ?: "Not connected"
        binding.tvWifiStatus.text = "Connected to: $currentSSID"
    }
    
    /**
     * Scan for available WiFi networks
     */
    private fun scanWiFiNetworks() {
        if (scanJob?.isActive == true) {
            showToast("Scan already in progress...")
            return
        }
        
        binding.btnScanWifi.isEnabled = false
        binding.btnScanWifi.text = "Scanning..."
        
        scanJob = lifecycleScope.launch {
            try {
                val networks = wifiManager.scanNetworks()
                
                if (networks.isEmpty()) {
                    showToast("No WiFi networks found")
                } else {
                    // Update spinner with networks
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        networks
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWifiNetworks.adapter = adapter
                    
                    showToast("Found ${networks.size} networks")
                }
            } catch (e: Exception) {
                showToast("Error scanning networks: ${e.message}")
            } finally {
                binding.btnScanWifi.isEnabled = true
                binding.btnScanWifi.text = "Scan WiFi Networks"
            }
        }
    }
    
    /**
     * Connect to LED_control WiFi network
     */
    private fun connectToLEDControl() {
        binding.btnConnectLedControl.isEnabled = false
        binding.btnConnectLedControl.text = "Connecting..."
        
        lifecycleScope.launch {
            try {
                // Get password from input field if provided
                val password = binding.etWifiPassword.text.toString()
                
                val connected = wifiManager.connectToNetwork("LED_control", password)
                
                if (connected) {
                    showToast("Connecting to LED_control...")
                    // Wait a bit for WiFi to connect
                    delay(2000)
                    updateWiFiStatus()
                } else {
                    showToast("Failed to initiate connection")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            } finally {
                binding.btnConnectLedControl.isEnabled = true
                binding.btnConnectLedControl.text = "Connect to LED_control"
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        brightnessJob?.cancel()
        scanJob?.cancel()
        _binding = null
    }
}
