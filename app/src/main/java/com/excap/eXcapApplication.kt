package com.excap

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class eXcapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Set dark theme as default
        val prefs = getSharedPreferences("excap_settings", MODE_PRIVATE)
        val darkTheme = prefs.getBoolean("dark_theme", true)
        
        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
