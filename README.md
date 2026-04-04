# ESP32 Control App - Android Application

An Android application built with Kotlin for controlling an ESP32-based LED strip over **Bluetooth Classic (SPP)**.

## Project Structure

```
ESP32ControlApp/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   ├── src/main/
│   │   ├── kotlin/com/example/esp32control/
│   │   │   ├── MainActivity.kt                  # Main activity — holds shared BluetoothConnectionManager
│   │   │   ├── ui/
│   │   │   │   ├── BluetoothFragment.kt         # Connect/disconnect UI, paired device list
│   │   │   │   ├── ModesFragment.kt             # Lighting modes control
│   │   │   │   └── SettingsFragment.kt          # Brightness slider and device settings
│   │   │   ├── network/
│   │   │   │   ├── BluetoothConnectionManager.kt  # Bluetooth Classic SPP connection logic
│   │   │   │   ├── ESP32Api.kt                  # Retrofit API interface (legacy WiFi)
│   │   │   │   ├── ESP32ApiClient.kt            # Retrofit client singleton (legacy WiFi)
│   │   │   │   └── WiFiConnectionManager.kt     # WiFi utilities
│   │   │   └── models/
│   │   │       └── Command.kt                   # Data models
│   │   ├── res/layout/
│   │   │   ├── activity_main.xml
│   │   │   ├── fragment_bluetooth.xml
│   │   │   ├── fragment_modes.xml
│   │   │   └── fragment_settings.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts (root)
├── settings.gradle.kts
└── .gitignore
```

## Technology Stack

- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)
- **Minimum SDK**: Android 7.0+ (API 24)
- **Target SDK**: Android 14 (API 34)
- **Bluetooth**: Classic Bluetooth, Serial Port Profile (SPP) via RFCOMM
- **UI**: Fragment + ViewPager2, TabLayout, Material Design
- **Async**: Kotlin Coroutines

## How Bluetooth Works

### Protocol: Classic Bluetooth SPP (not BLE/GATT)

The app uses **Bluetooth Classic with the Serial Port Profile (SPP)**. This emulates a serial cable over Bluetooth — data is streamed as raw bytes, not GATT characteristics.

- **UUID**: `00001101-0000-1000-8000-00805F9B34FB` (standard SPP UUID)
- **Socket type**: RFCOMM (`createRfcommSocketToServiceRecord`)
- **ESP32 library**: `BluetoothSerial` (Classic, not BLE)
- **ESP32 device name**: `LED_Control`

### Connection Flow

1. The ESP32 advertises itself as `LED_Control` via `SerialBT.begin("LED_Control")`
2. You pair the ESP32 with your Android phone once via Android Settings → Bluetooth
3. On app launch, `BluetoothFragment` searches paired devices for `LED_Control`
4. If found, it automatically opens an RFCOMM socket to it
5. The single `BluetoothConnectionManager` instance (owned by `MainActivity`) is shared across all fragments

> The device must be **pre-paired** in Android system settings. The app does not handle pairing — it only connects to already-paired devices.

### Sending Commands

Commands are JSON objects serialized to a string, terminated with `\n`, and written to the socket's output stream:

**Android side** (`BluetoothConnectionManager.kt`):
```kotlin
val commandWithNewline = "$command\n"
outputStream?.write(commandWithNewline.toByteArray())
outputStream?.flush()
```

**ESP32 side** (`Podsvetka-test.ino`):
```cpp
String command = SerialBT.readStringUntil('\n');
command.trim();
processCommand(command);
```

### Command Format

All commands are JSON with a `command` field and a `value` field:

```json
{"command": "color_mode", "value": "static_light"}
{"command": "color_mode", "value": "rainbow"}
{"command": "color_mode", "value": "pulse"}
{"command": "brightness", "value": 200}
{"command": "power", "value": "on"}
{"command": "power", "value": "off"}
```

### Response Format

The ESP32 replies with a JSON string over the same socket:

```json
{"status": "success", "message": "Rainbow mode enabled"}
{"status": "error", "message": "Unknown command or invalid value"}
```

### Shared Manager Architecture

`BluetoothConnectionManager` is instantiated once in `MainActivity` and accessed by all fragments via:

```kotlin
val bluetoothManager = (requireActivity() as MainActivity).bluetoothManager
```

This ensures `BluetoothFragment`, `ModesFragment`, and `SettingsFragment` all share the same socket connection. Creating separate instances per-fragment would break the connection state.

## Features

### Tab 1: Bluetooth
- Lists paired Bluetooth devices
- Auto-connects to `LED_Control` on launch
- Manual connect / disconnect buttons

### Tab 2: Modes
- Static Light, Rainbow, Pulse mode buttons
- Power on/off

### Tab 3: Settings
- Brightness slider (0–255) with 300ms debounce

## Quick Start

### Prerequisites
- Android Studio (latest)
- JDK 11+
- Android SDK API 34
- ESP32 flashed with `Podsvetka-test.ino`

### Setup

1. Flash `for-tests/Podsvetka-test.ino` to the ESP32
2. Pair your Android phone with `LED_Control` in Android Settings → Bluetooth
3. Open the project in Android Studio and sync Gradle
4. Run the app — it will auto-connect on the Bluetooth tab

### Building

```bash
./gradlew assembleDebug     # Debug build
./gradlew assembleRelease   # Release build
./gradlew installDebug      # Install on connected device
```

## Permissions

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />  <!-- Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />     <!-- Android 12+ -->
```

`BLUETOOTH_CONNECT` and `BLUETOOTH_SCAN` require runtime approval on Android 12+. The app requests these when the Bluetooth tab is opened.

## ESP32 Firmware

File: `for-tests/Podsvetka-test.ino`

| Detail | Value |
|--------|-------|
| Bluetooth library | `BluetoothSerial.h` |
| Device name | `LED_Control` |
| LED pin | GPIO 13 |
| LED count | 59 WS2812B |
| JSON library | ArduinoJson |

## Notes

- Brightness updates are debounced by 300ms to avoid flooding the ESP32 while dragging the slider
- All Bluetooth I/O runs on `Dispatchers.IO` coroutines to avoid blocking the UI thread
- View Binding is enabled for type-safe view access
