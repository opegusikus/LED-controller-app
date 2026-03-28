package com.example.esp32control.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.esp32control.R
import com.example.esp32control.network.BluetoothConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Bluetooth connection and control fragment
 * Allows user to discover, pair, and connect to ESP32 via Bluetooth
 */
class BluetoothFragment : Fragment() {
    
    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 200
    }
    
    private lateinit var bluetoothManager: BluetoothConnectionManager
    private lateinit var statusTextView: TextView
    private lateinit var devicesListView: ListView
    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var refreshButton: Button
    
    private var selectedDevice: String? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Request Bluetooth permissions first
        requestBluetoothPermissions()
        
        // Initialize Bluetooth manager
        bluetoothManager = BluetoothConnectionManager(requireContext())
        
        // Initialize UI elements
        statusTextView = view.findViewById(R.id.statusTextView)
        devicesListView = view.findViewById(R.id.devicesListView)
        connectButton = view.findViewById(R.id.connectButton)
        disconnectButton = view.findViewById(R.id.disconnectButton)
        refreshButton = view.findViewById(R.id.refreshButton)
        
        // Set up button listeners
        connectButton.setOnClickListener { connectToDevice() }
        disconnectButton.setOnClickListener { disconnectDevice() }
        refreshButton.setOnClickListener { refreshDeviceList() }
        
        // Set up device list click listener
        devicesListView.setOnItemClickListener { _, _, position, _ ->
            val devices = bluetoothManager.getPairedDevices()
            if (position < devices.size) {
                selectedDevice = devices[position].name
                showToast("Selected: $selectedDevice")
            }
        }
        
        // Check Bluetooth availability
        if (!bluetoothManager.isBluetoothAvailable()) {
            updateStatus("Bluetooth not available on this device")
            connectButton.isEnabled = false
            return
        }
        
        if (!bluetoothManager.isBluetoothEnabled()) {
            updateStatus("Bluetooth is disabled. Please enable it in settings.")
            connectButton.isEnabled = false
            return
        }
        
        // Try auto-connect to LED_Control device
        autoConnectToLEDControl()
        
        // Refresh device list
        refreshDeviceList()
    }
    
    /**
     * Request Bluetooth permissions required for Android 12+
     */
    private fun requestBluetoothPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
        
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                missingPermissions.toTypedArray(),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Auto-connect to LED_Control device if available
     */
    private fun autoConnectToLEDControl() {
        CoroutineScope(Dispatchers.Main).launch {
            val ledControlDevice = bluetoothManager.findLEDControlDevice()
            if (ledControlDevice != null) {
                updateStatus("Found LED_Control device. Attempting auto-connect...")
                val connected = bluetoothManager.connectToLEDControl()
                if (connected) {
                    updateStatus("✓ Connected to LED_Control")
                    connectButton.isEnabled = false
                    disconnectButton.isEnabled = true
                } else {
                    updateStatus("Failed to auto-connect to LED_Control")
                }
            } else {
                updateStatus("LED_Control device not found. Please pair it first.")
            }
        }
    }
    
    /**
     * Refresh the list of paired devices
     */
    private fun refreshDeviceList() {
        val devices = bluetoothManager.getPairedDevices()
        
        if (devices.isEmpty()) {
            updateStatus("No paired devices found")
            return
        }
        
        val deviceNames = devices.map { "${it.name} (${it.address})" }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            deviceNames
        )
        devicesListView.adapter = adapter
        
        updateStatus("Found ${devices.size} paired device(s)")
    }
    
    /**
     * Connect to selected device
     */
    private fun connectToDevice() {
        if (selectedDevice == null) {
            showToast("Please select a device first")
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            updateStatus("Connecting to $selectedDevice...")
            
            val devices = bluetoothManager.getPairedDevices()
            val device = devices.find { it.name == selectedDevice }
            
            if (device != null) {
                val connected = bluetoothManager.connectToDevice(device)
                if (connected) {
                    updateStatus("✓ Connected to $selectedDevice")
                    connectButton.isEnabled = false
                    disconnectButton.isEnabled = true
                } else {
                    updateStatus("✗ Failed to connect to $selectedDevice")
                    connectButton.isEnabled = true
                    disconnectButton.isEnabled = false
                }
            } else {
                updateStatus("Device not found")
            }
        }
    }
    
    /**
     * Disconnect from device
     */
    private fun disconnectDevice() {
        bluetoothManager.disconnect()
        updateStatus("Disconnected")
        connectButton.isEnabled = true
        disconnectButton.isEnabled = false
        selectedDevice = null
    }
    
    /**
     * Update status text view
     */
    private fun updateStatus(message: String) {
        statusTextView.text = message
    }
    
    /**
     * Show toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
