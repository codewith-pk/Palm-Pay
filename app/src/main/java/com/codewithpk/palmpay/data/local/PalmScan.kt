package com.codewithpk.palmpay.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "palm_scans")
data class PalmScan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // To link scan to a user (mock user for now)
    val scanTimestamp: Long,
    val imageUrl: String? = null, // Path to the captured palm image if stored locally
    val metadata: String? = null // Any additional metadata from analysis
)