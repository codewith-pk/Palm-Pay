package com.codewithpk.palmpay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PalmScan::class, EnrolledPalm::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun palmScanDao(): PalmScanDao
    abstract fun enrolledPalmDao(): EnrolledPalmDao
}