package com.codewithpk.palmpay.ui.scan


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithpk.palmpay.data.local.EnrolledPalm
import com.codewithpk.palmpay.data.local.PalmScan
import com.codewithpk.palmpay.data.repository.PalmScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val palmScanRepository: PalmScanRepository
) : ViewModel() {

    // For saving mock transaction scans
    fun savePalmScan(userId: String, imageUrl: String? = null, metadata: String? = null) {
        viewModelScope.launch {
            val palmScan = PalmScan(
                userId = userId,
                scanTimestamp = System.currentTimeMillis(),
                imageUrl = imageUrl,
                metadata = metadata
            )
            palmScanRepository.insertPalmScan(palmScan)
        }
    }

    // For enrolling a palm (new)
    fun enrollPalm(userId: String, palmImagePath: String, mockFeatureData: String) {
        viewModelScope.launch {
            val enrolledPalm = EnrolledPalm(
                userId = userId,
                palmImagePath = palmImagePath,
                mockFeatureData = mockFeatureData,
                enrollmentTimestamp = System.currentTimeMillis()
            )
            palmScanRepository.enrollPalm(enrolledPalm)
        }
    }

    // To retrieve the enrolled palm for simulated matching (Day 2)
    val enrolledPalm: Flow<EnrolledPalm?> = palmScanRepository.getAnyEnrolledPalm() // For hackathon, just get one

    // You can add logic here to expose a state for matching process later
    // val isMatching: MutableStateFlow<Boolean> = MutableStateFlow(false)
    // val matchResult: MutableStateFlow<Boolean?> = MutableStateFlow(null)
}