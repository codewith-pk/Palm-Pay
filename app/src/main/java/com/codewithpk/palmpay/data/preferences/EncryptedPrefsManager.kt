package com.codewithpk.palmpay.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class EncryptedPrefsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs", // File name
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Key for our mock login status
    private val KEY_IS_LOGGED_IN = "is_logged_in"

    // Use StateFlow to observe changes to login status
    private val _isLoggedIn = MutableStateFlow(sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun setLoggedIn(loggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
        _isLoggedIn.update { loggedIn } // Update the StateFlow
    }

    fun getLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearAuthData() {
        sharedPreferences.edit().clear().apply()
        _isLoggedIn.update { false }
    }
}