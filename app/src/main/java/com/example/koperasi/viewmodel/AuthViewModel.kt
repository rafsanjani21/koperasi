package com.example.koperasi.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koperasi.TokenManager
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.utils.LocationHelper
import com.example.koperasi.utils.isLocationEnabled
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
    fun loginGoogleWithLocation(
        context: Context,
        idToken: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {

            _isLoading.value = true

            try {
                // Cek GPS aktif
                if (!context.isLocationEnabled()) {
                    _errorMessage.value = "GPS belum aktif. Silakan aktifkan lokasi."
                    _isLoading.value = false
                return@launch
                    }

                // 🔥 1️⃣ Ambil lokasi dengan timeout 10 detik
                val location = try {
                    withTimeout(10_000L) {
                        LocationHelper.getCurrentLocation(context)
                    }
                } catch (e: TimeoutCancellationException) {
                    null
                }

                if (location.isNullOrEmpty()) {
                    _errorMessage.value = "Gagal mendapatkan lokasi. Aktifkan GPS dan coba lagi."
                    _isLoading.value = false
                    return@launch
                }

                // 🔥 2️⃣ Login ke backend
                repo.loginGoogle(idToken, location)

                // 🔥 3️⃣ Success
                onSuccess()

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login gagal"
            } finally {
                _isLoading.value = false
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

