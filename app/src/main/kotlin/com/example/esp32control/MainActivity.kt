package com.example.esp32control

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.esp32control.databinding.ActivityMainBinding
import com.example.esp32control.network.ESP32ApiClient
import com.example.esp32control.ui.ModesFragment
import com.example.esp32control.ui.SettingsFragment
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        requestRequiredPermissions()
        setupViewController()
        initializeESP32Connection()
    }
    
    /**
     * Request WiFi and location permissions required for WiFi scanning and connection
     */
    private fun requestRequiredPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
        )
        
        // Add location permissions for WiFi scanning (required on Android 6.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
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
                0 -> "Modes"
                1 -> "Settings"
                else -> "Unknown"
            }
        }.attach()
    }
    
    private fun initializeESP32Connection() {
        // Initialize the API client with device IP
        // TODO: Load IP from SharedPreferences or settings
        // For now, using default IP - can be changed in Settings
        try {
            ESP32ApiClient.setup(
                deviceIp = "192.168.1.100",
                port = 80,
                debugMode = true
            )
            showToast("Connected to ESP32")
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
        
        override fun getItemCount(): Int = 2
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ModesFragment()
                1 -> SettingsFragment()
                else -> Fragment()
            }
        }
    }
}
