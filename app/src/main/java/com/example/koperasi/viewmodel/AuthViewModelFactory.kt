package com.example.koperasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.koperasi.data.AuthRepository

/**
 * Factory untuk membuat instance AuthViewModel.
 *
 * Digunakan ketika ViewModel memiliki constructor parameter
 * (dalam hal ini AuthRepository), sehingga tidak bisa dibuat
 * langsung oleh ViewModelProvider default.
 *
 * Factory ini memastikan:
 * - Dependency (Repository) diinjeksi dengan benar
 * - ViewModel dibuat sesuai tipe yang diminta
 *
 * Biasanya dipakai bersama:
 * ViewModelProvider(activity, AuthViewModelFactory(repo))
 */
class AuthViewModelFactory(
    private val repo: AuthRepository
) : ViewModelProvider.Factory {

    /**
     * Membuat instance ViewModel berdasarkan class yang diminta.
     *
     * @param modelClass Class ViewModel yang diminta oleh sistem
     * @return Instance ViewModel yang sesuai
     *
     * @throws IllegalArgumentException jika ViewModel tidak dikenali
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repo) as T
        }
        // Lempar exception jika ViewModel tidak terdaftar
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
