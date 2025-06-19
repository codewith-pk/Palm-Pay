package com.codewithpk.palmpay.data.repository

import com.codewithpk.palmpay.data.local.EnrolledPalm
import com.codewithpk.palmpay.data.local.PalmScan
import com.codewithpk.palmpay.data.local.PalmScanDao
import com.codewithpk.palmpay.data.local.EnrolledPalmDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PalmScanRepository @Inject constructor(
    private val palmScanDao: PalmScanDao,
    private val enrolledPalmDao: EnrolledPalmDao
) {
    suspend fun insertPalmScan(palmScan: PalmScan) {
        palmScanDao.insertPalmScan(palmScan)
    }

    fun getAllPalmScans(): Flow<List<PalmScan>> {
        return palmScanDao.getAllPalmScans()
    }

    // New: Get the count of all palm scans/transactions
    fun getTotalScanCount(): Flow<Int> {
        return palmScanDao.getAllPalmScans().map { it.size }
    }

    // New: Get the latest successful payment amount
    fun getLastSuccessfulPayment(): Flow<PalmScan?> {
        // Filter for successful payments and get the latest one
        return palmScanDao.getAllPalmScans().map { scans ->
            scans.filter { it.metadata?.contains("Payment to", ignoreCase = true) == true && it.amount > 0 }
                .maxByOrNull { it.scanTimestamp }
        }
    }

    // For enrolled palm data (new for real-time feature)
    suspend fun enrollPalm(enrolledPalm: EnrolledPalm) {
        enrolledPalmDao.insertEnrolledPalm(enrolledPalm)
    }

    fun getEnrolledPalm(userId: String): Flow<EnrolledPalm?> {
        return enrolledPalmDao.getEnrolledPalmByUserId(userId)
    }

    fun getAnyEnrolledPalm(): Flow<EnrolledPalm?> {
        return enrolledPalmDao.getAnyEnrolledPalm()
    }
}