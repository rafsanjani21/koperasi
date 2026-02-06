package com.example.koperasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.koperasi.TokenManager
import com.example.koperasi.data.AuthRepository

class AuthViewModelFactory(
    private val repo: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                repo = repo,
                tokenManager = tokenManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

