package com.example.koperasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koperasi.TokenManager
import com.example.koperasi.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola proses autentikasi pengguna.
 *
 * Tanggung jawab utama:
 * - Menjembatani UI (Compose) dengan AuthRepository
 * - Menjalankan proses login secara asynchronous (coroutine)
 * - Menyediakan state error ke UI menggunakan StateFlow
 *
 * ViewModel ini bersifat lifecycle-aware dan aman dari memory leak
 * karena coroutine dijalankan di viewModelScope.
 *
 * @property repo Repository autentikasi (login / logout)
 */
class AuthViewModel(
    private val repo: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    /* =========================================================
     * STATE MANAGEMENT
     * ========================================================= */

    /**
     * State internal untuk menyimpan pesan error.
     * MutableStateFlow hanya bisa diubah dari dalam ViewModel.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)

    /**
     * State publik (read-only) yang diobservasi oleh UI.
     */
    val errorMessage: StateFlow<String?> = _errorMessage

    /* =========================================================
     * ACTIONS
     * ========================================================= */

    /**
     * Melakukan login menggunakan Google ID Token.
     *
     * Alur:
     * 1. UI memanggil fungsi ini setelah Google Sign-In berhasil
     * 2. Repository melakukan request login ke backend
     * 3. Jika sukses → onSuccess dipanggil
     * 4. Jika gagal → pesan error dikirim ke UI
     *
     * @param idToken Google ID Token hasil autentikasi Firebase
     * @param location Informasi lokasi / device pengguna
     * @param onSuccess Callback ketika login berhasil
     */
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

    fun logout() {
        viewModelScope.launch {
            try {
                repo.logout() // 🔥 API LOGOUT
            } catch (e: Exception) {
                // optional: log error
            } finally {
                repo.clearSession() // 🔥 clear token
            }
        }
    }


    /* =========================================================
     * ERROR HANDLING
     * ========================================================= */

    /**
     * Mengatur pesan error secara manual dari UI
     * (misalnya validasi input sebelum login).
     */
    fun setError(msg: String) {
        _errorMessage.value = msg
    }

    /**
     * Menghapus pesan error setelah ditampilkan di UI.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
