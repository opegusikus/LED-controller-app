package com.example.esp32control.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esp32control.ui.AppViewModel

@Composable
fun PowerButtons(
    onTurnOn: () -> Unit,
    onTurnOff: () -> Unit
) {
    Row(
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxWidth()
) {
    Button(
        onClick = onTurnOn,
        modifier = Modifier.weight(1f)
    ) {
        Text("Turn On")
    }
    Button(
        onClick = onTurnOff,
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