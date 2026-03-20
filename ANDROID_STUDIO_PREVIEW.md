# Android Studio Preview Guide

## 🎨 Previewing Your ESP32 Control App

Your app is now fully configured for Android Studio layout previews. Here's how to use them:

## Quick Start - Open in Android Studio

### 1. Open Android Studio
- Launch Android Studio
- Open the project: `File → Open...`
- Navigate to: `ESP32ControlApp/`
- Click "Open"

### 2. Wait for Gradle Sync
- Android Studio will automatically sync Gradle files
- This may take 1-2 minutes on first load
- You'll see "Gradle sync finished" in the status bar

### 3. Preview the Layouts

#### Main Activity Preview
1. Open: `app/src/main/res/layout/activity_main.xml`
2. Click the **"Design"** tab (bottom of editor)
3. You'll see the main layout with TabLayout and ViewPager2

#### Modes Fragment Preview
1. Open: `app/src/main/res/layout/fragment_modes.xml`
2. Click the **"Design"** tab
3. See the LED mode control buttons (Static Light, Rainbow, Pulse)

#### Settings Fragment Preview
1. Open: `app/src/main/res/layout/fragment_settings.xml`
2. Click the **"Design"** tab
3. View the brightness slider and device settings controls

## 📱 Preview Features

### Layout Inspector
- **Design Tab**: Shows visual representation of layouts
- **Blueprint Tab**: Shows wireframe view (easier to see structure)
- **Zoom Controls**: Adjust preview size (top-right of preview panel)
- **Device Configuration**: Change preview device (Pixel 4, Pixel 6, etc.)

### Rotate & Multi-Device Preview
- **Rotate Preview**: Press `Ctrl+R` (Windows) or `Cmd+R` (Mac)
- **Multi-Device Preview**: `Design → Multi-Preview` to see multiple device sizes
- **Tablet Preview**: Select tablet device from dropdown

### Color Scheme
- **Day/Night Mode**: Switch preview between light and dark themes
- **Localization**: Test different languages (if localized strings added)

## 🚀 Running on Device or Emulator

### Using Android Emulator
1. **Create Virtual Device**:
   - `Tools → Device Manager → Create Device`
   - Select device (e.g., Pixel 4)
   - Select Android 14 (API 34) API Level
   - Click "Create"

2. **Launch Emulator**:
   - `Tools → Device Manager`
   - Click the ▶️ (play) button next to your device

3. **Run App**:
   - Click **"Run"** button (▶️ icon) in toolbar
   - Select emulator
   - Wait for build and app launch

### Using Physical Android Device
1. **Enable Developer Mode**:
   - Settings → About Phone → Tap "Build Number" 7 times
   - Go back → Developer Options
   - Enable "USB Debugging"

2. **Connect Device**:
   - Plug in USB cable
   - Allow connection prompt on device

3. **Run App**:
   - Click **"Run"** button in Android Studio
   - Select your device
   - App will build and install

## 🎯 Preview Configuration

### Current Settings
- **Target Android**: 14 (API 34) ✅
- **Minimum Android**: 7.0 (API 24) ✅
- **Theme**: Material Design ✅
- **View Binding**: Enabled ✅
- **Layout Preview**: tools:context configured ✅

### Preview Display Sizes
Layouts are optimized for these preview sizes:
- **Phone**: 320dp width (typical mobile)
- **Tablet**: 600dp+ width
- **Landscape**: Rotate any preview

## 🔍 Troubleshooting Preview Issues

### Preview Won't Show
**Problem**: Design tab grayed out or shows error

**Solutions**:
1. Sync Gradle: `File → Sync Now`
2. Clean project: `Build → Clean Project`
3. Restart Android Studio: `File → Restart`
4. Invalidate cache: `File → Invalidate Caches → Invalidate and Restart`

### Missing Layout Resources
**Problem**: Preview shows errors about missing strings or colors

**Check**:
- Confirm `strings.xml` exists in `app/src/main/res/values/`
- Confirm `colors.xml` exists in `app/src/main/res/values/`
- Confirm `themes.xml` exists in `app/src/main/res/values/`
- Run Gradle sync again

### Wrong Preview Device
**Problem**: Preview doesn't match intended device size

**Fix**:
1. In Design panel, find device selector dropdown (top-right)
2. Select desired device (Pixel 4, Pixel 6, etc.)
3. Or create custom device size

### Dark Mode Not Working
**Problem**: Dark theme preview not available

**Fix**:
1. In Design panel, look for theme selector icon (sun/moon)
2. Toggle between Light and Dark
3. Or check themes.xml for dark variant configuration

## 🎨 Editing and Hot Reload

### Live Preview Updates
1. Make layout changes in XML editor
2. Preview updates automatically (may take 2-3 seconds)
3. Use "Refresh" button (top-right) if needed

### Quick Layout Changes
- Edit spacing: Change `android:padding="16dp"` values
- Adjust colors: Update values in `colors.xml`
- Modify text: Update strings in `strings.xml`
- All changes show in preview immediately

## 📋 Layout Files Structure

```
app/src/main/res/
├── layout/                          (Auto-preview in Designer)
│   ├── activity_main.xml            → Main screen with tabs
│   ├── fragment_modes.xml           → LED mode controls
│   └── fragment_settings.xml        → Brightness & settings
│
└── values/                          (Resources referenced in layouts)
    ├── strings.xml                  → Text content
    ├── colors.xml                   → Color definitions
    └── themes.xml                   → Material Design themes
```

## ✅ What's Configured for Preview

- ✅ `tools:context` attributes on all layouts
- ✅ All string resources (@string/...)
- ✅ All color resources (@color/...)
- ✅ Theme attributes applied
- ✅ View Binding enabled for live editing
- ✅ Material Design components ready

## 🚀 Next Steps

1. **Open in Android Studio** - Start the IDE
2. **Sync Gradle** - Wait for indexing
3. **View Layout Preview** - Open any XML and click Design tab
4. **Modify and See Changes** - Edit XML, preview updates live
5. **Run on Emulator/Device** - Test the actual app

## 💡 Pro Tips

- **Keyboard Shortcuts**:
  - `Ctrl+B` (Windows) / `Cmd+B` (Mac) - Go to declaration
  - `Ctrl+Space` - Code completion for XML attributes
  - `Alt+Enter` - Quick fixes

- **Layout Tools**:
  - Use Constraint Layout helper for responsive designs
  - Preview shows all device orientations
  - Multi-view allows side-by-side comparison

- **Performance**:
  - Gradle parallel build enabled for faster builds
  - Build cache speedy: `gradle.parallel=true`
  - Incremental compilation saves time on changes

---

**Your app is ready for development!** 🚀

Open the project in Android Studio and start designing your UI with live preview.
