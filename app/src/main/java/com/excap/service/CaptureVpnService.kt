package com.excap.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Stub VPN Service - needs proper implementation
 * Original had dependencies on non-existent DB methods and resources
 */
class CaptureVpnService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.excap.START_CAPTURE"
        const val ACTION_STOP = "com.excap.STOP_CAPTURE"
    }
}
