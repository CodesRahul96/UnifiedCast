# 📺 UnifiedCast — Zero-Cloud Remote, Touchpad & Clipboard Sync Engine

UnifiedCast is an ultra-low latency, zero-cloud remote controller, multi-touch trackpad, and universal clipboard bridge designed for Android, Android TV, and Linux (Zorin OS / Ubuntu / Windows). Built natively with Kotlin & Jetpack Compose, UnifiedCast turns your mobile phone into an intuitive control hub for your desktop and Smart TV ecosystem over local Wi-Fi without relying on third-party cloud servers.

## 🌟 Core Features & Technical Highlights

### ⚡ 1. Low-Latency Multi-Touch Trackpad
- **Sub-10ms Input Pipeline**: High-polling rate gesture tracking for fluid cursor motion using relative coordinate delta streaming.
- **Haptic Feedback Engine**: Tactile physical feedback for clicks, double-taps, drag-and-drop, and two-finger scrolling.
- **Customizable Pointer Sensitivity**: Adjustable acceleration curves matching desktop cursor speeds.

### 📋 2. Universal Cross-Device Clipboard Sync
- **Instant Copy-Paste**: Copy text on your Android phone and paste it directly onto your PC/Linux terminal or Android TV (and vice versa).
- **Background Clipboard Listener**: Low-power background service that syncs copied links, code snippets, and text strings automatically over encrypted local sockets.

### 📡 3. Zero-Cloud Security & Auto-Discovery
- **mDNS / Zeroconf Automatic Pairing**: Automatically discovers receiver endpoints on local Wi-Fi — no manual IP entry required.
- **100% Offline & Private**: Zero third-party telemetry, cloud relays, or external servers. All data stays strictly within your local network.
- **TLS Socket Encryption**: Secured local WebSocket/gRPC transport preventing local network packet sniffing.

### 🎬 4. Android TV & Desktop App Launcher
- **App Dashboard**: View and launch installed desktop applications or Android TV apps directly from your mobile screen.
- **Media Controls**: System-level media keys (Play/Pause, Track Skip, Master Volume Control) with OS-level volume integration.

---

## 🚀 Roadmap & Capabilities

| Phase 1: Core Foundation 🟢 | Phase 2: AI & Streaming 🟡 | Phase 3: Hardware Hub 🔵 |
|---|---|---|
| Jetpack Compose Touchpad | Low-Latency WebRTC Mirror | BLE / USB-OTG Backup |
| mDNS Local Auto-Discovery | AI Gyro-Pointer (AirMouse) | Rust Desktop Daemon |
| Universal Clipboard Sync | Biometric PIN Encryption | Smart Home Plugin System |

---

## 🛠️ Technology Stack & Architecture

- **Mobile Client**: 100% Native Kotlin, Jetpack Compose, Material Design 3, Coroutines, Flow.
- **Network Transport**: OkHttp WebSockets, gRPC, mDNS / NSD (Network Service Discovery).
- **Desktop Daemon**: Node.js / Rust, robotjs / uinput for native OS input virtualization.
- **Protocol Formats**: Asynchronous JSON/Protobuf binary messages for minimal payload serialization overhead.

---

## 📄 License & Author

- **Developer**: CodesRahul96 (Rahul Misal)
- **Portfolio**: [codesrahul.in](https://codesrahul.in)
- **License**: MIT License
