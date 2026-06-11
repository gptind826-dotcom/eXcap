# eXcap Changelog

All notable changes to the eXcap project are documented in this file.

## [2.0.0] - 2026-06-12

### Complete Redesign - Built by eXU CODER

This release represents a complete rebranding and UI/UX redesign of the original PCAPdroid codebase, migrating to a modern Jetpack Compose interface while preserving all core packet capture functionality.

### Added
- **Complete Jetpack Compose UI** - Full migration from XML layouts to declarative Compose
- **Material Design 3** - Dynamic theming support with dark-first default
- **Bottom Navigation** - Four main tabs: Capture, Connections, Stats, Settings
- **Animated Waveform Visualizer** - Real-time animated capture visualization
- **Animated Donut Charts** - Protocol distribution visualization
- **Bandwidth Graph** - Real-time traffic visualization with Canvas
- **Connection Cards** - Swipeable cards with protocol icons and metadata
- **Search & Filter** - Full-text search with protocol filter chips
- **Haptic Feedback** - Capture toggle and gesture haptics
- **Empty States** - Loading skeletons for all async operations
- **Splash Screen** - Branded splash on app launch
- **Capture Engine Kotlin Wrapper** - Native JNI bridge for zdtun integration
- **nDPI Wrapper** - Deep packet inspection Kotlin bindings
- **PCAP/PCAPNG Exporter** - Full export functionality with HTTP server
- **TLS Decryption Manager** - Certificate handling and mitmproxy integration
- **HTTP Inspector** - Request/response parsing with syntax highlighting
- **Native CMake Build** - JNI integration with existing native libraries

### Changed
- **Complete Rebranding** - All references changed from "PCAPdroid" to "eXcap"
- **New Color Scheme** - Dark teal and electric blue palette
- **New Launcher Icon** - Modern icon with lightning bolt and network nodes
- **Theme System** - Migrated to Material 3 dynamic theming
- **Navigation** - Migrated from drawer to bottom navigation
- **Architecture** - Enhanced MVVM with Compose integration
- **Build System** - Updated to Gradle 8.7 with Compose support

### Preserved (from PCAPdroid)
- VPN-based packet capture without root
- Native zdtun TCP/IP stack
- nDPI deep packet inspection engine
- Real-time connection tracking
- App-level traffic filtering
- Protocol filtering (HTTP, HTTPS, TCP, UDP, DNS)
- PCAP/PCAPng export formats
- TLS decryption via user-installed CA
- mitmproxy integration
- HTTP server for file download
- All import/export formats

### Build System
- Added GitHub Actions CI/CD pipeline by eXU CODER
- Automated APK builds on push to main
- Release builds with signing support
- Gradle dependency caching
- NDK integration for native builds

---

## [1.0.0] - 2026-06-10

### Initial Release
- Base Kotlin Android application
- Room database for packet storage
- Basic VPN capture service
- Material Design 3 components
- Navigation drawer UI
- Dashboard, Capture, Apps, Packets, Analysis, Settings screens

---

**Built by eXU CODER**

Original PCAPdroid project by Emanuele Faranda - https://github.com/emanuele-f/PCAPdroid
