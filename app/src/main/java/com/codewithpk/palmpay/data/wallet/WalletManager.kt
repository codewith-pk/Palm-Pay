package com.codewithpk.palmpay.data.wallet

/**
 * Created by @codewithpk
 * Date: 20/06/25
 * Time: 12:42 am
 */

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Create a DataStore instance for your preferences
val Context.walletDataStore: DataStore<Preferences> by preferencesDataStore(name = "wallet_prefs")

@Singleton
class WalletManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val CURRENT_BALANCE = doublePreferencesKey("current_balance")
    }

    val currentBalance: Flow<Double> = context.walletDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_BALANCE] ?: 00.00 // Default balance
        }

    suspend fun updateBalance(amount: Double) {
        context.walletDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CURRENT_BALANCE] ?: 0.00
            preferences[PreferencesKeys.CURRENT_BALANCE] = current + amount
        }
    }

    suspend fun resetBalance() {
        context.walletDataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_BALANCE] = 0.00 // Reset to default
        }
    }
}