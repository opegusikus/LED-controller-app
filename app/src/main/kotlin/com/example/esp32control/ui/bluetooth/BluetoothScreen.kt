package com.example.esp32control.ui.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esp32control.data.ConnectionState
import com.example.esp32control.network.BluetoothConnectionManager

@Composable
fun BluetoothScreen(modifier: Modifier = Modifier) {
    val viewModel: BluetoothViewModel = viewModel()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var scanStatus by remember { mutableStateOf("") }
    var foundDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val bluetoothAdapter = remember {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    // BroadcastReceiver for discovery — registered and unregistered with the composable lifecycle
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = IntentCompat.getParcelableExtra(
                            intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                        )
                        if (device?.name == BluetoothConnectionManager.DEVICE_NAME) {
                            bluetoothAdapter?.cancelDiscovery()
                            foundDevice = device
                            scanStatus = "Found nearby:"
                            isScanning = false
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        isScanning = false
                        if (foundDevice == null) scanStatus = "Device not found nearby"
                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        val device = IntentCompat.getParcelableExtra(
                            intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                        )
                        val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                        if (device?.name == BluetoothConnectionManager.DEVICE_NAME
                            && bondState == BluetoothDevice.BOND_BONDED
                        ) {
                            foundDevice = null
                            scanStatus = "Paired! Connecting..."
                            viewModel.connect()
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            bluetoothAdapter?.cancelDiscovery()
            context.unregisterReceiver(receiver)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isScanning = true
            foundDevice = null
            scanStatus = "Scanning..."
            bluetoothAdapter?.startDiscovery()
        } else {
            scanStatus = "Location permission required to scan"
        }
    }

    fun startScan() {
        val hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocation) {
            isScanning = true
            foundDevice = null
            scanStatus = "Scanning..."
            bluetoothAdapter?.startDiscovery()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "LED Controller",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Device row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Status dot
            Surface(
                shape = CircleShape,
                color = when (connectionState) {
                    ConnectionState.Connected -> Color(0xFF4CAF50)
                    ConnectionState.Connecting -> Color(0xFFFFC107)
                    else -> Color(0xFF666666)
                },
                modifier = Modifier.size(12.dp)
            ) {}

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = BluetoothConnectionManager.DEVICE_NAME,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (connectionState) {
                        ConnectionState.Connected -> "Connected"
                        ConnectionState.Connecting -> "Connecting..."
                        ConnectionState.Failed -> statusMessage
                        ConnectionState.Idle -> statusMessage.ifEmpty { "Not connected" }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (connectionState) {
                ConnectionState.Connected -> OutlinedButton(onClick = { viewModel.disconnect() }) {
                    Text("Disconnect")
                }
                ConnectionState.Connecting -> OutlinedButton(onClick = {}, enabled = false) {
                    Text("Connecting...")
                }
                else -> Button(onClick = { viewModel.connect() }) {
                    Text("Connect")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        // Discovery section
        Text(
            text = "Not seeing the device?",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedButton(
            onClick = { startScan() },
            enabled = !isScanning,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(if (isScanning) "Scanning..." else "Scan nearby")
        }

        if (scanStatus.isNotEmpty()) {
            Text(
                text = scanStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Found unpaired device
        AnimatedVisibility(visible = foundDevice != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = foundDevice?.name ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Found nearby — not paired",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { foundDevice?.createBond() }) {
                    Text("Pair")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
