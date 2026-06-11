package com.excap.database

import androidx.room.*
import com.excap.model.ConnectionInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections WHERE isActive = 1 ORDER BY startTime DESC")
    fun getActiveConnections(): Flow<List<ConnectionInfo>>

    @Query("SELECT * FROM connections ORDER BY startTime DESC LIMIT :limit")
    fun getRecentConnections(limit: Int = 500): Flow<List<ConnectionInfo>>

    @Query("SELECT * FROM connections WHERE packageName = :packageName ORDER BY startTime DESC")
    fun getConnectionsByApp(packageName: String): Flow<List<ConnectionInfo>>

    @Query("SELECT * FROM connections WHERE connectionId = :connectionId")
    suspend fun getConnectionById(connectionId: String): ConnectionInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: ConnectionInfo)

    @Query("UPDATE connections SET isActive = 0, endTime = :endTime WHERE connectionId = :connectionId")
    suspend fun closeConnection(connectionId: String, endTime: Long = System.currentTimeMillis())

    @Query("UPDATE connections SET bytesSent = bytesSent + :bytes WHERE connectionId = :connectionId")
    suspend fun addBytesSent(connectionId: String, bytes: Long)

    @Query("UPDATE connections SET bytesReceived = bytesReceived + :bytes WHERE connectionId = :connectionId")
    suspend fun addBytesReceived(connectionId: String, bytes: Long)

    @Query("DELETE FROM connections WHERE startTime < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM connections")
    suspend fun deleteAll()
}
