package com.excap.database

import android.content.Context

/**
 * Stub database class - Room removed due to kapt incompatibility with AGP 9.x
 * Will be re-implemented with KSP in future update
 */
class AppDatabase {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase()
                INSTANCE = instance
                instance
            }
        }
    }
}
