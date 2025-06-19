package com.codewithpk.palmpay.data.repository

import com.codewithpk.palmpay.data.local.EnrolledPalm
import com.codewithpk.palmpay.data.local.PalmScan
import com.codewithpk.palmpay.data.local.PalmScanDao
import com.codewithpk.palmpay.data.local.EnrolledPalmDao
import kotlinx.coroutines.flow.Flow
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