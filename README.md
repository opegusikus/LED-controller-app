# ESP32 Control App - Android Application

A complete Android application built with Kotlin and Gradle for controlling an ESP32 device over WiFi using HTTPS + JSON protocol.

## Project Structure

```
ESP32ControlApp/
├── app/
│   ├── build.gradle.kts                         # App-level Gradle configuration
│   ├── proguard-rules.pro                       # ProGuard rules for minification
│   ├── src/main/
│   │   ├── kotlin/com/example/esp32control/
│   │   │   ├── MainActivity.kt                  # Main activity with tab navigation
│   │   │   ├── ui/
│   │   │   │   ├── ModesFragment.kt             # Lighting modes control
│   │   │   │   └── SettingsFragment.kt          # Device settings (brightness, IP)
│   │   │   ├── network/
│   │   │   │   ├── ESP32Api.kt                  # Retrofit API interface
│   │   │   │   └── ESP32ApiClient.kt            # Retrofit client singleton
│   │   │   └── models/
│   │   │       └── Command.kt                   # Data models (Command, Response, etc)
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml            # Main activity layout
│   │   │   │   ├── fragment_modes.xml           # Modes fragment layout
│   │   │   │   └── fragment_settings.xml        # Settings fragment layout
│   │   │   ├── values/
│   │   │   │   ├── strings.xml                  # String resources
│   │   │   │   ├── colors.xml                   # Color definitions
│   │   │   │   └── themes.xml                   # Theme definitions
│   │   │   └── drawable/                        # Drawable resources (icons, images)
│   │   └── AndroidManifest.xml                  # Android manifest
│   └── build.gradle.kts (root)
├── settings.gradle.kts                          # Gradle settings
└── .gitignore                                   # Git ignore rules
```

## Technology Stack

- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)
- **Minimum SDK**: Android 7.0+ (API 24)
- **Target SDK**: Android 14 (API 34)
- **UI Components**:
  - Fragment + ViewPager2 for tab-based navigation
  - TabLayout for tab indicators
  - Material Design components
- **HTTP Client**: Retrofit2 + OkHttp3 (configured for HTTPS)
- **JSON Serialization**: Gson
- **Architecture**: MVVM-ready with LiveData
- **Async**: Kotlin Coroutines

## Dependencies

### Core Android
- `androidx.core:core-ktx` - Kotlin extensions
- `androidx.appcompat:appcompat` - Legacy support
- `com.google.android.material:material` - Material Design components

### UI Framework
- `androidx.viewpager2:viewpager2` - ViewPager2 for tab navigation
- `androidx.fragment:fragment-ktx` - Fragment support with Kotlin extensions
- `androidx.lifecycle:lifecycle-runtime-ktx` - Lifecycle awareness
- `androidx.lifecycle:lifecycle-viewmodel-ktx` - ViewModel support
- `androidx.lifecycle:lifecycle-livedata-ktx` - LiveData support

### Network
- `com.squareup.retrofit2:retrofit` - HTTP client framework
- `com.squareup.retrofit2:converter-gson` - JSON converter for Retrofit
- `com.squareup.okhttp3:okhttp` - HTTP client
- `com.squareup.okhttp3:logging-interceptor` - HTTP request/response logging

### Serialization
- `com.google.code.gson:gson` - JSON serialization/deserialization

### Concurrency
- `org.jetbrains.kotlinx:kotlinx-coroutines-core` - Coroutines core
- `org.jetbrains.kotlinx:kotlinx-coroutines-android` - Android coroutines support

## Features

### Tab 1: Modes
- **Static Light**: Send command to display static light
- **Rainbow**: Activate rainbow mode
- **Pulse**: Enable pulsing light effect
- **Power Control**: Turn device on/off

### Tab 2: Settings
- **Brightness Slider**: 0-255 range with real-time updates to ESP32
- **Device IP Configuration**: Update ESP32 device IP address (uses HTTPS:443)
- **Refresh State**: Fetch current device state

## Communication Protocol

The app communicates with the ESP32 using HTTPS POST requests with JSON payloads:

```json
POST https://[ESP32_IP]:443/api/command
{
  "command": "color_mode",
  "value": "static_light"
}
```

### Endpoints
- `POST /api/command` - Send control commands
- `GET /api/state` - Get current device state
- `GET /api/info` - Get device information

### Response Format
```json
{
  "status": "success",
  "message": "Command executed",
  "data": { ... }
}
```

## Quick Start

### Prerequisites
- Android Studio (latest)
- JDK 11 or higher
- Android SDK (API level 34)
- ESP32 device with web server firmware supporting HTTPS/SSL

### Building the App

1. Open the project in Android Studio
2. Sync Gradle files: `File → Sync Now`
3. Build the project: `Build → Make Project`
4. Run on device/emulator: `Run → Run 'app'`

### Configuration

Edit the default IP address in `MainActivity.kt`:
```kotlin
ESP32ApiClient.setup(
    deviceIp = "192.168.4.1",  // Default ESP32 AP IP
    port = 443,                // Using HTTPS port
    debugMode = true
)
```

Or update it in the Settings tab at runtime.

## ESP32 Firmware Requirements

Your ESP32 needs to implement:

1. **Web Server**: Listen on port 443 (HTTPS)
2. **SSL/TLS**: Support secure connections (the app is configured to trust self-signed certs for local IPs)
3. **API Endpoint**: `POST /api/command` - Accept JSON commands
4. **JSON Parsing**: Parse commands and values
5. **LED Control**: Control LEDs based on received commands
6. **Response Format**: Return JSON responses

### Example ESP32 Commands Handled
- `{"command": "color_mode", "value": "static_light"}`
- `{"command": "color_mode", "value": "rainbow"}`
- `{"command": "color_mode", "value": "pulse"}`
- `{"command": "brightness", "value": 128}`
- `{"command": "power", "value": "on"}` or `"off"`

## Error Handling

- Connection timeouts: 10 seconds
- Automatic retry on network errors
- User-friendly error messages via Toast notifications
- HTTPS request/response logging (enabled in debug mode)
- Self-signed certificate support for local network device connection

## Future Enhancements

- [ ] SharedPreferences for storing favorite settings
- [ ] Real-time state synchronization with LiveData
- [ ] Color picker UI for RGB control
- [ ] Animation effect presets
- [ ] Device discovery via mDNS
- [ ] Local network detection and auto-connection
- [ ] Wake-on-LAN support
- [ ] Schedule/automation features

## Building & Deployment

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Running on Device
```bash
./gradlew installDebug
```

## Testing

Unit tests and UI tests can be added to:
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - UI/Integration tests

## Permissions

The app requires the following permissions:
- `android.permission.INTERNET` - For HTTPS communication
- `android.permission.ACCESS_NETWORK_STATE` - For network state checking

## License

This project is part of the Podsvetka backlight control system.

## Notes

- The app uses a singleton pattern for ESP32ApiClient to manage HTTPS connections
- Brightness updates are debounced by 300ms to reduce API calls while sliding
- All network calls are made on coroutine dispatchers to avoid blocking the UI
- View Binding is enabled for type-safe view access
