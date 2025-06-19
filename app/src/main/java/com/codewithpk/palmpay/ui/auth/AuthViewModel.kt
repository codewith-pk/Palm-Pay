package com.codewithpk.palmpay.ui.auth

/**
 * Created by @codewithpk
 * Date: 20/06/25
 * Time: 12:35 am
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithpk.palmpay.data.preferences.EncryptedPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val encryptedPrefsManager: EncryptedPrefsManager
) : ViewModel() {

    val isLoggedIn = encryptedPrefsManager.isLoggedIn

    fun setLoggedIn(loggedIn: Boolean) {
        viewModelScope.launch {
            encryptedPrefsManager.setLoggedIn(loggedIn)
        }
    }

    fun logout() {
        viewModelScope.launch {
            encryptedPrefsManager.clearAuthData()
        }
    }
}