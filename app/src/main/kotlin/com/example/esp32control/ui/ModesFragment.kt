package com.example.esp32control.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.esp32control.databinding.FragmentModesBinding
import com.example.esp32control.models.Command
import com.example.esp32control.network.BluetoothConnectionManager
import com.example.esp32control.network.ESP32ApiClient
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Fragment for controlling lighting modes
 * Provides buttons for different LED modes (static, rainbow, etc.)
 */
class ModesFragment : Fragment() {
    
    private var _binding: FragmentModesBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Static light mode button
        binding.btnStaticLight.setOnClickListener {
            sendCommand("color_mode", "static_light")
        }
        
        // Rainbow mode button
        binding.btnRainbow.setOnClickListener {
            sendCommand("color_mode", "rainbow")
        }
        
        // Pulse mode button
        binding.btnPulse.setOnClickListener {
            sendCommand("color_mode", "pulse")
        }
        
        // Turn off button
        binding.btnTurnOff.setOnClickListener {
            sendCommand("power", "off")
        }
        
        // Turn on button
        binding.btnTurnOn.setOnClickListener {
            sendCommand("power", "on")
        }
    }
    
    /**
     * Send a command to the ESP32 via Bluetooth
     * @param command The command type
     * @param value The command value
     */
    private fun sendCommand(command: String, value: Any) {
        lifecycleScope.launch {
            try {
                val bluetoothManager = BluetoothConnectionManager(requireContext())
                
                // Check if connected
                if (!bluetoothManager.isConnected()) {
                    showToast("Not connected to ESP32. Please connect via Bluetooth tab.")
                    return@launch
                }
                
                // Create JSON command
                val jsonCommand = JSONObject().apply {
                    put("command", command)
                    put("value", value)
                }
                
                // Send via Bluetooth
                val success = bluetoothManager.sendCommand(jsonCommand.toString())
                
                if (success) {
                    showToast("$command: Command sent successfully")
                } else {
                    showToast("Error: Failed to send command via Bluetooth")
                }
                
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
