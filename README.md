# 📺 UnifiedCast — Premium Smart TV Remote, Touchpad & Media Controller

UnifiedCast is a high-performance, zero-cloud remote controller, multi-touch trackpad, dynamic TV app launcher, and universal keyboard bridge designed for Android TV, Fire TV, and Smart TV ecosystems over local Wi-Fi. Built natively with **Kotlin**, **Jetpack Compose**, and **Material Design 3**, UnifiedCast turns your Android phone into a sleek, matte dark control hub without relying on external cloud servers.

[![Latest Release](https://img.shields.io/github/v/release/CodesRahul96/UnifiedCast?color=00E5FF&label=Latest%20Release)](https://github.com/CodesRahul96/UnifiedCast/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Android%20TV-green)](https://developer.android.com)

---

## 🌟 Key Features & Highlights

### 🎯 1. Dual Control Mode: D-Pad & Fluid Touchpad
- **Ultra-Responsive D-Pad Ring**: Dedicated Directional Pad with tactile spring animations and quick-access **OK** selection.
- **Glassmorphic Touchpad Surface**: Swipe gestures for 4-way navigation (`Up`, `Down`, `Left`, `Right`) and single-tap for selection.
- **Toggle Mode Switcher**: Seamlessly switch between tactile D-Pad buttons and fluid Touchpad gestures with a single tap.

### 🔊 2. Home Screen & Dedicated Media Controls
- **Quick-Access Home Controls**: Integrated **Volume (VOL)** and **Channel (CH)** control pills directly on the D-Pad Home tab.
- **Media Control Center**: Dedicated tab with Rewind, Play/Pause, Fast-Forward, Previous/Next Track, Volume Up/Down, Channel Up/Down, and Mute buttons.
- **System Power & Navigation**: Quick buttons for Power, Input Selection, Settings, Info, Back, Home, Menu, and TV Guide.

### 📱 3. 1-Tap TV App Launcher
- **Instant TV Shortcuts**: One-tap launching for popular TV apps including **YouTube, Netflix, Prime Video, Disney+ Hotstar, Zee5, JioCinema, SonyLIV, Spotify**, and System Settings.
- **Firebase Remote Config Sync**: App shortcuts automatically sync and update dynamically via Remote Config.

### 📡 4. Multi-Protocol Smart TV Scanner
- **Subnet Auto-Discovery**: Fast parallel IP scanning across your local Wi-Fi subnet (`192.168.x.x`).
- **Multi-Port TV Detection**: Scans ADB Wireless (`5555`), Android TV Remote Service (`6466`), Chromecast (`8008`/`7000`), and Web Controls (`8080`).
- **MAC OUI Brand Matching**: Automatically identifies TV hardware vendors including **Amazon Fire TV, Sony Bravia, Nvidia Shield, Xiaomi Mi TV Box, TCL Google TV, Samsung Smart TV**, and **LG webOS TV**.

### ⌨️ 5. TV Text Input & Keyboard Bridge
- **Remote Typing**: Type text on your smartphone keyboard and transmit strings directly to TV search boxes and input fields.
- **Quick Actions**: One-tap clear text and quick send controls.

---

## 🎨 UI/UX Aesthetic

- **Matte Dark Design System**: Sleek black (`#0F172A`) container with custom `SurfaceDark` (`#1E1E1E`) cards.
- **Cyan Accent Theme**: High-contrast `AccentCyan` (`#00E5FF`) borders, active pills, and status badges.
- **Horizontal Swipe Navigation**: Swipe left or right anywhere on screen to switch between **Media Controls, TV Apps, Remote Home, Keyboard, and Settings**.

---

## 🛠️ Technology Stack & Architecture

- **Language & Framework**: 100% Native Kotlin, Jetpack Compose, Material Design 3.
- **Architecture**: Component-based modular architecture (`ui/components/`, `models/`, `network/`).
- **Networking**: `java.net.Socket` ADB-over-TCP protocol, `SubnetScanner` with parallel coroutines, `TvDiscovery` mDNS.
- **Dynamic Config**: Firebase Remote Config integration for remote app launcher shortcuts.

---

## 📥 Installation & Latest Release

You can download the latest pre-built APK directly from the Releases page:

👉 **[Download UnifiedCast v1.0.0 APK](https://github.com/CodesRahul96/UnifiedCast/releases/latest)**

### Local Building
```bash
# Clone the repository
git clone https://github.com/CodesRahul96/UnifiedCast.git
cd UnifiedCast/android

# Build Debug APK
./gradlew installDebug

# Build Release APK
./gradlew assembleRelease
```

---

## 📄 License & Author

- **Developer**: Rahul Misal ([CodesRahul96](https://github.com/CodesRahul96))
- **Portfolio**: [codesrahul.in](https://codesrahul.in)
- **License**: MIT License
