package com.excap.model

sealed class CaptureEvent {
    data class PacketCaptured(val packet: PacketInfo) : CaptureEvent()
    data class ConnectionOpened(val connection: ConnectionInfo) : CaptureEvent()
    data class ConnectionClosed(val connectionId: String, val bytesTransferred: Long) : CaptureEvent()
    data class StatsUpdated(val stats: AppTrafficStats) : CaptureEvent()
    data class Error(val message: String) : CaptureEvent()
    object CaptureStarted : CaptureEvent()
    object CaptureStopped : CaptureEvent()
}

enum class Protocol {
    TCP, UDP, HTTP, HTTPS, DNS, TLS, QUIC, UNKNOWN;
    
    companion object {
        fun fromString(value: String): Protocol {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}

enum class PacketDirection {
    INCOMING, OUTGOING
}

enum class CaptureAction {
    CAPTURE, IGNORE, BLOCK, LOG_ONLY
}

data class CaptureConfig(
    val captureHttp: Boolean = true,
    val captureHttps: Boolean = true,
    val captureTcp: Boolean = true,
    val captureUdp: Boolean = true,
    val captureDns: Boolean = true,
    val maxPayloadSize: Int = 65536,
    val bufferSize: Int = 8192,
    val ringBufferSize: Int = 10000,
    val selectedApps: List<String> = emptyList(),
    val blockedHosts: List<String> = emptyList(),
    val sslStripEnabled: Boolean = false,
    val decryptPayloads: Boolean = false,
    val autoScroll: Boolean = true,
    val showHexDump: Boolean = false,
    val darkTheme: Boolean = true
)
