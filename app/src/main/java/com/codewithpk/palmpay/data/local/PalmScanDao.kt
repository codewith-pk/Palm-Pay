package com.codewithpk.palmpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PalmScanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPalmScan(palmScan: PalmScan)

    // Flow allows observing changes to the data in real-time (useful for history screen)
    @Query("SELECT * FROM palm_scans ORDER BY scanTimestamp DESC")
    fun getAllPalmScans(): Flow<List<PalmScan>>
}