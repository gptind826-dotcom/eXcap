package com.excap.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.excap.R
import com.excap.database.AppDatabase
import com.excap.model.*
import com.excap.parser.PacketParser
import com.excap.ui.MainActivity
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class CaptureVpnService : VpnService() {

    companion object {
        private const val TAG = "CaptureVpnService"
        const val ACTION_START = "com.excap.START_CAPTURE"
        const val ACTION_STOP = "com.excap.STOP_CAPTURE"
        const val EXTRA_SELECTED_APPS = "selected_apps"
        const val NOTIFICATION_CHANNEL_ID = "excap_capture_channel"
        const val NOTIFICATION_ID = 1001
        
        @Volatile
        var isRunning = false
            private set
            
        @Volatile
        var packetCount = 0L
            private set
            
        @Volatile
        var bytesTransferred = 0L
            private set
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var captureJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isActive = AtomicBoolean(false)
    private val appUidMap = ConcurrentHashMap<Int, Pair<String, String>>()
    private var database: AppDatabase? = null
    private val _captureEvents = kotlinx.coroutines.channels.Channel<CaptureEvent>(1000)
    val captureEvents = _captureEvents

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        buildAppUidMap()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val selectedApps = intent.getStringArrayListExtra(EXTRA_SELECTED_APPS) ?: arrayListOf()
                startCapture(selectedApps)
            }
            ACTION_STOP -> stopCapture()
        }
        return START_STICKY
    }

    private fun buildAppUidMap() {
        try {
            val pm = packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            packages.forEach { app ->
                try {
                    val name = pm.getApplicationLabel(app).toString()
                    appUidMap[app.uid] = Pair(app.packageName, name)
                } catch (e: Exception) {
                    // Skip apps we can't read
                }
            }
            Log.d(TAG, "Mapped ${appUidMap.size} apps")
        } catch (e: Exception) {
            Log.e(TAG, "Error building app map", e)
        }
    }

    private fun startCapture(selectedApps: List<String>) {
        if (isRunning) return

        isRunning = true
        isActive.set(true)

        try {
            val builder = Builder()
                .setSession("eXcap")
                .setMtu(1500)
                .addAddress("10.0.0.2", 24)
                .addDnsServer("8.8.8.8")
                .addRoute("0.0.0.0", 0)

            // Allow/disallow specific apps
            if (selectedApps.isNotEmpty()) {
                // Add allowed applications
                selectedApps.forEach { pkg ->
                    try {
                        builder.addAllowedApplication(pkg)
                    } catch (e: PackageManager.NameNotFoundException) {
                        Log.w(TAG, "App not found: $pkg")
                    }
                }
            }

            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                startForeground(NOTIFICATION_ID, buildNotification())
                captureJob = serviceScope.launch {
                    runPacketCapture()
                }
                broadcastEvent(CaptureEvent.CaptureStarted)
                Log.i(TAG, "Capture started successfully")
            } else {
                Log.e(TAG, "Failed to establish VPN interface")
                stopCapture()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting capture", e)
            stopCapture()
        }
    }

    private suspend fun runPacketCapture() {
        val vpnFd = vpnInterface ?: return
        val input = FileInputStream(vpnFd.fileDescriptor)
        val output = FileOutputStream(vpnFd.fileDescriptor)
        val buffer = ByteBuffer.allocate(32767)

        while (isActive.get()) {
            try {
                val length = input.read(buffer.array())
                if (length > 0) {
                    packetCount++
                    bytesTransferred += length
                    
                    // Parse and store packet
                    val rawData = buffer.array().copyOf(length)
                    
                    // Determine app from connection info (simplified)
                    val appInfo = getAppInfo(rawData)
                    
                    val parsed = PacketParser.parsePacket(
                        rawData, 
                        length, 
                        appInfo.first, 
                        appInfo.second
                    )
                    
                    parsed?.let { result ->
                        // Store in database
                        database?.packetDao()?.insertPacket(result.packetInfo)
                        
                        // Update app stats
                        updateAppStats(result.packetInfo)
                        
                        // Broadcast event
                        broadcastEvent(CaptureEvent.PacketCaptured(result.packetInfo))
                        
                        // Update connection info
                        updateConnection(result.packetInfo)
                    }
                    
                    // Forward packet
                    output.write(rawData, 0, length)
                    
                    buffer.clear()
                }
            } catch (e: Exception) {
                if (isActive.get()) {
                    Log.e(TAG, "Packet processing error", e)
                    broadcastEvent(CaptureEvent.Error(e.message ?: "Unknown error"))
                }
            }
        }
        
        try {
            input.close()
            output.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing streams", e)
        }
    }

    private fun getAppInfo(rawData: ByteArray): Pair<String, String> {
        // In a real implementation, this would use /proc/net/tcp to map connections to UIDs
        // For now, return unknown
        return Pair("unknown", "Unknown App")
    }

    private suspend fun updateAppStats(packet: PacketInfo) {
        val dao = database?.appStatsDao() ?: return
        val existing = dao.getAppStats(packet.appPackage)
        
        val stats = if (existing != null) {
            existing.copy(
                totalBytesSent = if (packet.direction == "OUTGOING") 
                    existing.totalBytesSent + packet.totalSize else existing.totalBytesSent,
                totalBytesReceived = if (packet.direction == "INCOMING") 
                    existing.totalBytesReceived + packet.totalSize else existing.totalBytesReceived,
                totalPackets = existing.totalPackets + 1,
                httpRequests = if (packet.protocol == "HTTP") existing.httpRequests + 1 else existing.httpRequests,
                httpsRequests = if (packet.protocol == "HTTPS") existing.httpsRequests + 1 else existing.httpsRequests,
                tcpConnections = if (packet.protocol == "TCP") existing.tcpConnections + 1 else existing.tcpConnections,
                lastSeen = System.currentTimeMillis()
            )
        } else {
            AppTrafficStats(
                packageName = packet.appPackage,
                appName = packet.appName,
                totalBytesSent = if (packet.direction == "OUTGOING") packet.totalSize else 0,
                totalBytesReceived = if (packet.direction == "INCOMING") packet.totalSize else 0,
                totalPackets = 1,
                httpRequests = if (packet.protocol == "HTTP") 1 else 0,
                httpsRequests = if (packet.protocol == "HTTPS") 1 else 0,
                tcpConnections = if (packet.protocol == "TCP") 1 else 0
            )
        }
        
        dao.insertAppStats(stats)
        broadcastEvent(CaptureEvent.StatsUpdated(stats))
    }

    private suspend fun updateConnection(packet: PacketInfo) {
        val dao = database?.connectionDao() ?: return
        val existing = dao.getConnectionById(packet.connectionId)
        
        if (existing == null) {
            val connection = ConnectionInfo(
                connectionId = packet.connectionId,
                packageName = packet.appPackage,
                appName = packet.appName,
                destinationHost = packet.destinationHost,
                destinationIp = packet.destinationIp,
                destinationPort = packet.destinationPort,
                protocol = packet.protocol,
                bytesSent = if (packet.direction == "OUTGOING") packet.totalSize else 0,
                bytesReceived = if (packet.direction == "INCOMING") packet.totalSize else 0,
                packetsSent = if (packet.direction == "OUTGOING") 1 else 0,
                packetsReceived = if (packet.direction == "INCOMING") 1 else 0
            )
            dao.insertConnection(connection)
            broadcastEvent(CaptureEvent.ConnectionOpened(connection))
        } else {
            if (packet.direction == "OUTGOING") {
                dao.addBytesSent(packet.connectionId, packet.totalSize)
            } else {
                dao.addBytesReceived(packet.connectionId, packet.totalSize)
            }
        }
    }

    private fun broadcastEvent(event: CaptureEvent) {
        serviceScope.launch {
            try {
                _captureEvents.send(event)
            } catch (e: Exception) {
                // Channel full, drop event
            }
        }
    }

    private fun stopCapture() {
        isActive.set(false)
        isRunning = false
        captureJob?.cancel()
        
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
        vpnInterface = null
        
        broadcastEvent(CaptureEvent.CaptureStopped)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        Log.i(TAG, "Capture stopped")
    }

    override fun onRevoke() {
        Log.w(TAG, "VPN permission revoked")
        stopCapture()
    }

    override fun onDestroy() {
        stopCapture()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "eXcap Packet Capture",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active network packet capture session"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, CaptureVpnService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("eXcap - Packet Capture Active")
            .setContentText("Monitoring network traffic...")
            .setSmallIcon(R.drawable.ic_capture_active)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_stop, "Stop Capture", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
