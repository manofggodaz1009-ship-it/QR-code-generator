package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QrHistoryDao {
    @Query("SELECT * FROM qr_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<QrHistoryEntity>>

    @Query("SELECT * FROM qr_history WHERE isGenerated = 1 ORDER BY timestamp DESC")
    fun getGeneratedHistory(): Flow<List<QrHistoryEntity>>

    @Query("SELECT * FROM qr_history WHERE isGenerated = 0 ORDER BY timestamp DESC")
    fun getScannedHistory(): Flow<List<QrHistoryEntity>>

    @Query("SELECT * FROM qr_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteHistory(): Flow<List<QrHistoryEntity>>

    @Query("SELECT * FROM qr_history WHERE type = 'UPI' ORDER BY timestamp DESC")
    fun getUpiHistory(): Flow<List<QrHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QrHistoryEntity): Long

    @Update
    suspend fun update(entity: QrHistoryEntity)

    @Delete
    suspend fun delete(entity: QrHistoryEntity)

    @Query("DELETE FROM qr_history")
    suspend fun clearAll()

    @Query("SELECT * FROM qr_history ORDER BY timestamp DESC")
    suspend fun getAllList(): List<QrHistoryEntity>
}
