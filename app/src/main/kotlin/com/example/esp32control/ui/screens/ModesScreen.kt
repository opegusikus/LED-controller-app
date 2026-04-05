package com.example.esp32control.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esp32control.ui.AppViewModel

@Composable
fun ModesScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
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
            onActivate = { viewModel.sendCommand("color_mode", "static_light") }
        ) {
            BrightnessPanel(viewModel)
        }

        Spacer(Modifier.height(12.dp))

        ModeButton(
            label = "Rainbow",
            onActivate = { viewModel.sendCommand("color_mode", "rainbow") }
        ) {
            BrightnessPanel(viewModel)
        }

        Spacer(Modifier.height(12.dp))

        ModeButton(
            label = "Pulse",
            onActivate = { viewModel.sendCommand("color_mode", "pulse") }
        ) {
            BrightnessPanel(viewModel)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        Text(
            text = "Power",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.sendCommand("power", "on") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Turn On")
            }
            Button(
                onClick = { viewModel.sendCommand("power", "off") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Turn Off")
            }
        }
    }
}

/**
 * A button that sends a mode command immediately on tap,
 * and toggles an expandable settings panel below it.
 */
@Composable
fun ModeButton(
    label: String,
    onActivate: () -> Unit,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Button(
            onClick = {
                onActivate()
                expanded = !expanded
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun BrightnessPanel(viewModel: AppViewModel) {
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
            // Send only when finger lifts — no debounce needed
            viewModel.sendCommand("brightness", (brightness * 255).toInt())
        },
        modifier = Modifier.fillMaxWidth()
    )
}
