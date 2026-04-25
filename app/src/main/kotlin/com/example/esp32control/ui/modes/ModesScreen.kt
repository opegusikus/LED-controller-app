package com.example.esp32control.ui.modes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ModesScreen(modifier: Modifier = Modifier) {
    val viewModel: ModesViewModel = viewModel()
    var expandedLabel by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Lighting Modes",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ModeButton(
            label = "Static Light",
            onActivate = { viewModel.sendCommand("color_mode", "static_light") },
            expanded = expandedLabel == "Static Light",
            onToggle = { expandedLabel = if (expandedLabel == "Static Light") null else "Static Light" }
        ) {
            BrightnessPanel(viewModel)
        }

        Spacer(Modifier.height(12.dp))

        ModeButton(
            label = "Rainbow",
            onActivate = { viewModel.sendCommand("color_mode", "rainbow") },
            expanded = expandedLabel == "Rainbow",
            onToggle = { expandedLabel = if (expandedLabel == "Rainbow") null else "Rainbow" }
        ) {
            BrightnessPanel(viewModel)
        }

        Spacer(Modifier.height(12.dp))

        ModeButton(
            label = "Pulse",
            onActivate = { viewModel.sendCommand("color_mode", "pulse") },
            expanded = expandedLabel == "Pulse",
            onToggle = { expandedLabel = if (expandedLabel == "Pulse") null else "Pulse" }
        ) {
            BrightnessPanel(viewModel)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        PowerButtons(
            onTurnOn = { viewModel.sendCommand("power", "on") },
            onTurnOff = { viewModel.sendCommand("power", "off") }
        )
    }
}

@Composable
fun BrightnessPanel(viewModel: ModesViewModel) {
    var brightness by remember { mutableFloatStateOf(0.5f) }

    Text(
        text = "Brightness — ${(brightness * 255).toInt()}",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Slider(
        value = brightness,
        onValueChange = { brightness = it },
        onValueChangeFinished = {
            viewModel.sendCommand("brightness", (brightness * 255).toInt())
        },
        modifier = Modifier.fillMaxWidth()
    )
}
