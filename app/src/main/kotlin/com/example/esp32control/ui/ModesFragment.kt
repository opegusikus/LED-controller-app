package com.example.esp32control.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.esp32control.MainActivity
import com.example.esp32control.databinding.FragmentModesBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class ModesFragment : Fragment() {

    private var _binding: FragmentModesBinding? = null
    private val binding get() = _binding!!

    private var brightnessJob: Job? = null

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
        setupBrightnessSlider()
    }

    private fun setupClickListeners() {
        binding.btnStaticLight.setOnClickListener { sendCommand("color_mode", "static_light") }
        binding.btnRainbow.setOnClickListener { sendCommand("color_mode", "rainbow") }
        binding.btnPulse.setOnClickListener { sendCommand("color_mode", "pulse") }
        binding.btnTurnOff.setOnClickListener { sendCommand("power", "off") }
        binding.btnTurnOn.setOnClickListener { sendCommand("power", "on") }
    }

    private fun setupBrightnessSlider() {
        binding.brightnessSlider.apply {
            min = 0
            max = 255
            progress = 128
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        binding.brightnessValue.text = "$progress"
                        sendBrightnessUpdate(progress)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) { brightnessJob?.cancel() }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun sendBrightnessUpdate(brightness: Int) {
        brightnessJob?.cancel()
        brightnessJob = lifecycleScope.launch {
            delay(300)
            sendCommand("brightness", brightness)
        }
    }

    private fun sendCommand(command: String, value: Any) {
        lifecycleScope.launch {
            try {
                val bluetoothManager = (requireActivity() as MainActivity).bluetoothManager

                if (!bluetoothManager.isConnected()) {
                    showToast("Not connected to ESP32. Please connect via Bluetooth tab.")
                    return@launch
                }

                val jsonCommand = JSONObject().apply {
                    put("command", command)
                    put("value", value)
                }

                val success = bluetoothManager.sendCommand(jsonCommand.toString())
                if (!success) {
                    showToast("Failed to send command")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        brightnessJob?.cancel()
        _binding = null
    }
}
