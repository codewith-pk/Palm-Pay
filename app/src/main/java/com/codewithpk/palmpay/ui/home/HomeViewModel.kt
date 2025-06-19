package com.codewithpk.palmpay.ui.home

/**
 * Created by @codewithpk
 * Date: 20/06/25
 * Time: 12:52 am
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithpk.palmpay.data.repository.PalmScanRepository
import com.codewithpk.palmpay.data.wallet.WalletManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val walletManager: WalletManager, private val palmScanRepository: PalmScanRepository
) : ViewModel() {

    val currentBalance: StateFlow<Double> = walletManager.currentBalance
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0 // Default initial balance
        )

    //  Total number of scans
    val totalScansMade: StateFlow<Int> =
        palmScanRepository.getTotalScanCount().distinctUntilChanged().stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

    //  Last successful payment amount and merchant
    val lastSuccessfulPaymentInfo: StateFlow<Pair<Double, String>?> =
        palmScanRepository.getLastSuccessfulPayment().map { palmScan ->
                if (palmScan != null) {
                    val merchantName = palmScan.metadata?.split(", ")?.find {
                        it.contains("Payment to", ignoreCase = true) || it.contains(
                            "Received from", ignoreCase = true
                        )
                    }?.substringAfter(" to ")?.substringAfter(" from ") ?: "Unknown"
                    Pair(palmScan.amount, merchantName)
                } else {
                    null
                }
            }.distinctUntilChanged().stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    // Function to handle balance update (e.g., after a successful payment simulation)
    fun deposit(amount: Double) {
        viewModelScope.launch {
            walletManager.updateBalance(amount)
        }
    }

    fun withdraw(amount: Double) {
        viewModelScope.launch {
            walletManager.updateBalance(-amount)
        }
    }

    fun resetBalance() {
        viewModelScope.launch {
            walletManager.resetBalance()
        }
    }
}