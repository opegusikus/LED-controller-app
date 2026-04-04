package com.example.esp32control.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.fragment.app.Fragment
import com.example.esp32control.MainActivity
import com.example.esp32control.R
import com.example.esp32control.network.BluetoothConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothFragment : Fragment() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
    }

    private lateinit var bluetoothManager: BluetoothConnectionManager
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private lateinit var statusDot: View
    private lateinit var tvDeviceStatus: TextView
    private lateinit var btnConnect: Button
    private lateinit var btnScan: Button
    private lateinit var tvScanStatus: TextView
    private lateinit var layoutFoundDevice: LinearLayout
    private lateinit var tvFoundDeviceName: TextView
    private lateinit var btnPair: Button

    private var foundUnpairedDevice: BluetoothDevice? = null

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = IntentCompat.getParcelableExtra(
                        intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                    )
                    if (device?.name == BluetoothConnectionManager.DEVICE_NAME) {
                        bluetoothAdapter?.cancelDiscovery()
                        showFoundDevice(device)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (foundUnpairedDevice == null) {
                        tvScanStatus.text = "Device not found nearby"
                        btnScan.isEnabled = true
                        btnScan.text = "Scan nearby"
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? = IntentCompat.getParcelableExtra(
                        intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                    )
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    if (device?.name == BluetoothConnectionManager.DEVICE_NAME
                        && bondState == BluetoothDevice.BOND_BONDED) {
                        layoutFoundDevice.visibility = View.GONE
                        tvScanStatus.text = "Paired! Connecting..."
                        connectToDevice()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetoothManager = (requireActivity() as MainActivity).bluetoothManager

        statusDot = view.findViewById(R.id.statusDot)
        tvDeviceStatus = view.findViewById(R.id.tvDeviceStatus)
        btnConnect = view.findViewById(R.id.btnConnect)
        btnScan = view.findViewById(R.id.btnScan)
        tvScanStatus = view.findViewById(R.id.tvScanStatus)
        layoutFoundDevice = view.findViewById(R.id.layoutFoundDevice)
        tvFoundDeviceName = view.findViewById(R.id.tvFoundDeviceName)
        btnPair = view.findViewById(R.id.btnPair)

        btnConnect.setOnClickListener { connectToDevice() }
        btnScan.setOnClickListener { startDiscovery() }
        btnPair.setOnClickListener { pairFoundDevice() }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        requireContext().registerReceiver(discoveryReceiver, filter)

        requestBluetoothPermissions()

        if (!bluetoothManager.isBluetoothAvailable()) {
            setStatus("Bluetooth not available", connected = false)
            btnConnect.isEnabled = false
            return
        }
        if (!bluetoothManager.isBluetoothEnabled()) {
            setStatus("Bluetooth is disabled", connected = false)
            btnConnect.isEnabled = false
            return
        }

        if (bluetoothManager.isConnected()) {
            setStatus("Connected", connected = true)
        } else {
            autoConnect()
        }
    }

    private fun autoConnect() {
        setStatus("Connecting...", connected = false)
        btnConnect.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            val deviceName = bluetoothManager.autoConnect()
            if (deviceName != null) {
                setStatus("Connected", connected = true)
            } else {
                val hasSavedMac = bluetoothManager.getSavedMac() != null
                if (hasSavedMac) {
                    setStatus("Device not in range", connected = false)
                } else {
                    setStatus("Not paired — scan to find device", connected = false)
                }
                btnConnect.isEnabled = true
            }
        }
    }

    private fun connectToDevice() {
        setStatus("Connecting...", connected = false)
        btnConnect.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            val deviceName = bluetoothManager.autoConnect()
            if (deviceName != null) {
                setStatus("Connected", connected = true)
            } else {
                setStatus("Connection failed", connected = false)
                btnConnect.isEnabled = true
            }
        }
    }

    private fun startDiscovery() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        foundUnpairedDevice = null
        layoutFoundDevice.visibility = View.GONE
        tvScanStatus.text = "Scanning..."
        btnScan.isEnabled = false
        btnScan.text = "Scanning..."

        bluetoothAdapter?.startDiscovery()
    }

    private fun showFoundDevice(device: BluetoothDevice) {
        foundUnpairedDevice = device
        tvFoundDeviceName.text = device.name
        layoutFoundDevice.visibility = View.VISIBLE
        tvScanStatus.text = "Found nearby:"
        btnScan.isEnabled = true
        btnScan.text = "Scan nearby"
    }

    private fun pairFoundDevice() {
        val device = foundUnpairedDevice ?: return
        tvScanStatus.text = "Pairing — confirm on your phone..."
        device.createBond()  // triggers system pairing dialog
    }

    private fun setStatus(message: String, connected: Boolean) {
        tvDeviceStatus.text = message
        statusDot.background.setTint(
            if (connected) Color.parseColor("#4CAF50") else Color.parseColor("#CCCCCC")
        )
        btnConnect.text = if (connected) "Disconnect" else "Connect"
        btnConnect.setOnClickListener {
            if (connected) {
                bluetoothManager.disconnect()
                setStatus("Disconnected", connected = false)
            } else {
                connectToDevice()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
        Toast.makeText(
            requireContext(),
            "Location permission required for Bluetooth scan",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun requestBluetoothPermissions() {
        val perms = mutableListOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), missing.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bluetoothAdapter?.cancelDiscovery()
        try {
            requireContext().unregisterReceiver(discoveryReceiver)
        } catch (_: Exception) {}
    }
}
