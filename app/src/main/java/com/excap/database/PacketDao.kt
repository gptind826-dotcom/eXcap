package com.excap.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.excap.model.PacketInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface PacketDao {
    @Query("SELECT * FROM captured_packets ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPackets(limit: Int = 1000): Flow<List<PacketInfo>>

    @Query("SELECT * FROM captured_packets WHERE appPackage = :packageName ORDER BY timestamp DESC LIMIT :limit")
    fun getPacketsByApp(packageName: String, limit: Int = 500): Flow<List<PacketInfo>>

    @Query("SELECT * FROM captured_packets WHERE protocol = :protocol ORDER BY timestamp DESC LIMIT :limit")
    fun getPacketsByProtocol(protocol: String, limit: Int = 500): Flow<List<PacketInfo>>

    @Query("SELECT * FROM captured_packets WHERE connectionId = :connectionId ORDER BY timestamp")
    fun getPacketsByConnection(connectionId: String): Flow<List<PacketInfo>>

    @Query("SELECT * FROM captured_packets WHERE id = :id")
    suspend fun getPacketById(id: Long): PacketInfo?

    @Query("SELECT * FROM captured_packets WHERE destinationHost LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' OR sourceIp LIKE '%' || :query || '%' OR destinationIp LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 500")
    fun searchPackets(query: String): Flow<List<PacketInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPacket(packet: PacketInfo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackets(packets: List<PacketInfo>)

    @Query("DELETE FROM captured_packets WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM captured_packets")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM captured_packets")
    fun getPacketCount(): Flow<Long>

    @Query("SELECT COUNT(*) FROM captured_packets WHERE appPackage = :packageName")
    fun getPacketCountByApp(packageName: String): Flow<Long>

    @Query("SELECT COUNT(*) FROM captured_packets WHERE timestamp > :since")
    suspend fun getRecentCount(since: Long): Long

    @Query("SELECT * FROM captured_packets ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPacketsPaginated(limit: Int, offset: Int): List<PacketInfo>
}
