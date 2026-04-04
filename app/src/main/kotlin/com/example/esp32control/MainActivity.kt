package com.example.esp32control

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.esp32control.databinding.ActivityMainBinding
import com.example.esp32control.network.BluetoothConnectionManager
import com.example.esp32control.network.ESP32ApiClient
import com.example.esp32control.ui.BluetoothFragment
import com.example.esp32control.ui.ModesFragment
import com.example.esp32control.ui.SettingsFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    lateinit var bluetoothManager: BluetoothConnectionManager

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothManager = BluetoothConnectionManager(this)
        requestRequiredPermissions()
        setupViewController()
        initializeESP32Connection()
    }
    
    /**
     * Request WiFi permissions required for WiFi scanning and connection
     */
    private fun requestRequiredPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
        )
        
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    @Deprecated("Deprecated in API 33")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }
            
            if (deniedPermissions.isNotEmpty()) {
                showToast("Some permissions were denied: ${deniedPermissions.joinToString()}")
            } else {
                showToast("All permissions granted!")
            }
        }
    }
    
    private fun setupViewController() {
        viewPager = binding.viewPager
        
        // Set up ViewPager2 adapter
        viewPager.adapter = FragmentPagerAdapter(this)
        
        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Bluetooth"
                1 -> "Modes"
                2 -> "Settings"
                else -> "Unknown"
            }
        }.attach()
    }
    
    private fun initializeESP32Connection() {
        // Initialize the API client with device IP
        // Default: ESP32 Access Point IP (192.168.4.1) with HTTPS on port 443
        // Can be changed in Settings if connecting as a WiFi client instead
        try {
            ESP32ApiClient.setup(
                deviceIp = "192.168.4.1",
                port = 443,
                debugMode = true
            )
            showToast("ESP32 API initialized (HTTPS)")
        } catch (e: Exception) {
            showToast("Failed to initialize ESP32 connection: ${e.message}")
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Fragment adapter for ViewPager2
     */
    private inner class FragmentPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        
        override fun getItemCount(): Int = 3
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> BluetoothFragment()
                1 -> ModesFragment()
                2 -> SettingsFragment()
                else -> Fragment()
            }
        }
    }
}
