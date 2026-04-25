package com.example.esp32control.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.esp32control.ui.bluetooth.BluetoothScreen
import com.example.esp32control.ui.modes.ModesScreen
import com.example.esp32control.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Bluetooth, contentDescription = null) },
                        label = { Text("Bluetooth") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                        label = { Text("Modes") }
                    )
                }
            }
        ) { innerPadding ->
            when (selectedTab) {
                0 -> BluetoothScreen(modifier = Modifier.padding(innerPadding))
                1 -> ModesScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
