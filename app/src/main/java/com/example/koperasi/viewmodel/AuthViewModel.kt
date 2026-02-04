package com.example.koperasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koperasi.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository   // ✅ WAJIB ADA
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loginGoogle(
        idToken: String,
        location: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repo.loginGoogle(idToken, location)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login gagal"
            }
        }
    }

    fun setError(msg: String) {
        _errorMessage.value = msg
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
