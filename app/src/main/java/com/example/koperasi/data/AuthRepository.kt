package com.example.koperasi.data

import android.util.Log
import com.example.koperasi.TokenManager
import com.example.koperasi.data.remote.*
import org.json.JSONObject

/**
 * AuthRepository
 *
 * Repository yang bertanggung jawab atas seluruh proses autentikasi pengguna,
 * termasuk:
 * - Login menggunakan Google (OAuth)
 * - Penyimpanan token autentikasi
 * - Logout dan invalidasi token di server
 *
 * Repository ini menjadi penghubung antara ViewModel dan API Service.
 *
 * @param api ApiService untuk komunikasi dengan backend
 * @param tokenManager TokenManager untuk menyimpan dan mengambil token secara lokal
 */
class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {

    /**
     * loginGoogle
     *
     * Melakukan proses login menggunakan Google ID Token.
     * Data yang dikirim ke backend:
     * - idToken hasil autentikasi Google
     * - lokasi pengguna (sebagai kebutuhan validasi / logging backend)
     *
     * Jika login berhasil:
     * - Access Token dan Token Hash disimpan ke TokenManager
     *
     * Jika login gagal:
     * - Error message dibaca dari response backend
     * - Exception dilempar untuk ditangani oleh ViewModel
     *
     * @param idToken Token autentikasi dari Google Sign-In
     * @param location Lokasi pengguna
     * @throws Exception Jika login gagal
     */
    suspend fun loginGoogle(idToken: String, location: String) {
        val res = api.loginGoogle(
            LoginRequest(idToken, location)
        )

        if (!res.isSuccessful) {
            val errorBody = res.errorBody()?.string()
            val msg = try {
                JSONObject(errorBody ?: "{}").optString("message", "Login gagal")
            } catch (e: Exception) {
                "Login gagal"
            }
            throw Exception(msg)
        }

        val body = res.body() ?: throw Exception("Response kosong")
        val data = body.data ?: throw Exception("Data login kosong")

        tokenManager.saveTokens(
            accessToken = data.accessToken,
            tokenHash = data.tokenHash
        )

        tokenManager.saveUserName(data.user.name)
    }


    /**
     * logout
     *
     * Melakukan proses logout pengguna.
     * - Mengambil access token dan token hash dari local storage
     * - Mengirim request logout ke backend
     *
     * Jika token tidak tersedia, fungsi akan langsung dihentikan.
     * Tidak mengembalikan nilai apapun karena logout bersifat best-effort.
     */
    suspend fun logout() {
        // Ambil token dari penyimpanan lokal
        val accessToken = tokenManager.getAccessToken() ?: return
        val tokenHash = tokenManager.getTokenHash() ?: return

        Log.d("AUTH", "LOGOUT API HIT")
        // Kirim request logout ke backend
        api.logout(
            bearer = "Bearer $accessToken",
            body = LogoutRequest(tokenHash)
        )
    }

    fun clearSession() {
        tokenManager.clearTokens()
    }
}
