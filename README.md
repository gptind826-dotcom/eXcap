# eXcap - Network Packet Capture & Analysis

**Professional-grade network packet capture tool for Android**

> Built by **eXU CODER** | Rebranded & Redesigned from PCAPdroid

[![Build eXcap APK](https://github.com/gptind826-dotcom/eXcap/actions/workflows/build-apk.yml/badge.svg)](https://github.com/gptind826-dotcom/eXcap/actions/workflows/build-apk.yml)
[![Android](https://img.shields.io/badge/Android-26%2B-brightgreen)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6-purple)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue)](COPYING)

---

## Overview

**eXcap** is a privacy-focused, open-source network packet capture and analysis application for Android. Originally forked from [PCAPdroid](https://github.com/emanuele-f/PCAPdroid) by [Emanuele Faranda](https://github.com/emanuele-f), eXcap has been completely redesigned with a modern Jetpack Compose UI, Material Design 3, and enhanced capture capabilities.

eXcap simulates a VPN to capture network traffic without requiring root access. All data is processed locally on the device -- nothing is sent to remote servers.

---

## Features

### Core Packet Capture
- **VPN-based Capture** - Intercepts all device traffic without root access
- **Real-time Monitoring** - Live packet streaming with minimal latency
- **Multi-Protocol Support** - TCP, UDP, HTTP, HTTPS, DNS, TLS
- **Deep Packet Inspection** - Full header parsing and payload analysis via nDPI
- **Hex Dump View** - Raw packet data in hexadecimal format
- **PCAP Export** - Dump traffic to PCAP/PCAPng files
- **HTTP Server** - Download captures directly from browser

### App Monitoring
- **Per-App Tracking** - Monitor network usage per application
- **App Filtering** - Select specific apps to capture or exclude
- **Traffic Statistics** - Bytes sent/received, packet counts per app
- **Connection Tracking** - Real-time active connection monitoring

### TLS Decryption
- **HTTPS Inspection** - Decrypt TLS traffic with user-installed CA certificate
- **SSLKEYLOGFILE Export** - Compatible with Wireshark
- **mitmproxy Integration** - Built-in proxy for TLS decryption

### Analysis & Visualization
- **Dashboard Overview** - Real-time traffic metrics and animated statistics
- **Protocol Distribution** - Animated donut charts showing traffic breakdown
- **Bandwidth Graph** - Real-time traffic visualization
- **Top Destinations** - Most contacted hosts and servers
- **Top Apps** - Apps ranked by data usage
- **Connection Timeline** - Chronological connection flow

### User Interface
- **Material Design 3** - Complete Jetpack Compose UI with dynamic theming
- **Dark-first Theme** - Optimized dark UI with optional light mode
- **Bottom Navigation** - Capture, Connections, Stats, Settings tabs
- **Animated Transitions** - Shared element transitions between screens
- **Haptic Feedback** - Capture start/stop and gesture feedback
- **Empty States** - Loading skeletons for all async operations

### Advanced Features
- **Root Capture** - On rooted devices, capture while other VPN apps run
- **Firewall** - Create rules to block apps, domains, and IPs
- **Malware Detection** - Detect malicious connections via blacklists
- **Offline Geolocation** - Identify country and ASN of remote servers
- **Packet Filtering** - Filter by protocol, app, IP, or search terms
- **Auto-start on Boot** - Optional capture on device startup

---

## Architecture

```
eXcap/
├── app/src/main/
│   ├── java/com/excap/
│   │   ├── service/           # VPN & Analyzer services
│   │   │   ├── CaptureVpnService.kt       # Main packet capture via VPN
│   │   │   ├── PacketAnalyzerService.kt   # Background analysis
│   │   │   └── BootReceiver.kt            # Boot startup
│   │   ├── parser/            # Packet parsing engine
│   │   ├── capture/           # Native capture integration
│   │   │   ├── CaptureEngine.kt           # zdtun integration
│   │   │   ├── NdpiWrapper.kt             # nDPI bindings
│   │   │   ├── PcapExporter.kt            # PCAP/PCAPng export
│   │   │   ├── TlsDecryptManager.kt       # TLS decryption
│   │   │   └── HttpInspector.kt           # HTTP inspection
│   │   ├── database/          # Room database
│   │   ├── model/             # Data models
│   │   ├── ui/                # Compose UI layer
│   │   │   ├── MainActivity.kt
│   │   │   ├── theme/         # Material 3 theme
│   │   │   ├── components/    # Reusable Compose components
│   │   │   ├── screens/       # Screen composables
│   │   │   │   ├── CaptureScreen.kt
│   │   │   │   ├── ConnectionsScreen.kt
│   │   │   │   ├── ConnectionDetailScreen.kt
│   │   │   │   ├── StatsScreen.kt
│   │   │   │   └── SettingsScreen.kt
│   │   │   └── viewmodel/     # ViewModels
│   │   ├── adapter/           # RecyclerView adapters (legacy)
│   │   └── utils/             # Utilities
│   ├── jni/                   # Native capture engine
│   │   ├── core/              # zdtun, nDPI integration
│   │   ├── third_party/       # nDPI, zstd
│   │   └── CMakeLists.txt
│   └── res/                   # Resources
└── .github/workflows/
    └── build-apk.yml          # CI/CD pipeline by eXU CODER
```

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.0 |
| UI Framework | Jetpack Compose + Material Design 3 |
| Architecture | MVVM with ViewModel + Flow |
| Database | Room (SQLite) |
| Native Engine | C/C++ (zdtun, nDPI) |
| Networking | VPN Service + Raw Sockets |
| Charts | Compose Canvas + MPAndroidChart |
| Coroutines | Kotlin Coroutines + Flow |
| Build System | Gradle 8.7 |
| CI/CD | GitHub Actions by eXU CODER |

---

## Building from Source

### Prerequisites
- Android Studio Iguana (2023.2.1) or newer
- JDK 17 or higher
- Android SDK 36
- NDK 28.2.13676358
- CMake 3.22.1
- Android device/emulator with API 26+

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/gptind826-dotcom/eXcap.git
   cd eXcap
   git submodule update --init
   ```

2. **Open in Android Studio**
   - Open the project in Android Studio
   - Install the appropriate SDK and NDK
   - Sync Gradle files

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Using GitHub Actions

The project includes a GitHub Actions CI/CD pipeline built by **eXU CODER** that automatically builds signed APKs:

1. Push to `main` or `develop` branch
2. Go to **Actions** tab in GitHub
3. Download the latest APK artifact

---

## Permissions

| Permission | Purpose |
|------------|---------|
| `VPN_SERVICE` | Core packet capture functionality |
| `INTERNET` | Network access |
| `ACCESS_NETWORK_STATE` | Monitor network connectivity |
| `ACCESS_WIFI_STATE` | WiFi state monitoring |
| `FOREGROUND_SERVICE` | Background capture service |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Special use foreground service |
| `QUERY_ALL_PACKAGES` | List installed apps for monitoring |
| `POST_NOTIFICATIONS` | Capture status notifications |
| `RECEIVE_BOOT_COMPLETED` | Auto-start on boot |
| `WRITE_EXTERNAL_STORAGE` | PCAP file export |

---

## How It Works

1. **VPN Setup**: eXcap creates a local VPN interface using Android's `VpnService` API
2. **Traffic Interception**: All network traffic is routed through the VPN interface
3. **Native Processing**: Raw IP packets are processed by the native zdtun TCP/IP stack
4. **Deep Inspection**: nDPI analyzes packets to extract protocol metadata
5. **Protocol Detection**: Port numbers and payload analysis identify HTTP/HTTPS/DNS traffic
6. **Data Storage**: Parsed packets are stored in Room database for analysis
7. **Real-time Display**: Kotlin Flow streams data to the Compose UI in real-time

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the GNU General Public License v3.0 - see the [COPYING](COPYING) file for details.

The original PCAPdroid project by Emanuele Faranda is also licensed under GPL v3.0.

---

## Acknowledgments

- **Emanuele Faranda** - Original author of [PCAPdroid](https://github.com/emanuele-f/PCAPdroid)
- **nDPI** - Deep packet inspection library by ntop
- **zdtun** - Minimal TCP/IP stack by Emanuele Faranda
- **mitmproxy** - TLS decryption proxy
- **eXU CODER** - Rebranding, UI/UX redesign, and CI/CD pipeline

---

<p align="center">
  <b>eXcap</b> - Capture. Analyze. Understand.<br>
  <small>Built by <b>eXU CODER</b></small>
</p>
