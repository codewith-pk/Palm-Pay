package com.codewithpk.palmpay.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithpk.palmpay.data.local.PalmScan
import com.codewithpk.palmpay.data.repository.PalmScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    palmScanRepository: PalmScanRepository
) : ViewModel() {

    val allPalmScans: StateFlow<List<PalmScan>> = palmScanRepository.getAllPalmScans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep collecting for 5 seconds after last subscriber
            initialValue = emptyList()
        )
}