package com.example.koperasi.data

import android.util.Log
import com.example.koperasi.TokenManager
import com.example.koperasi.data.remote.ApiService
import com.example.koperasi.data.remote.RefreshResponse
import retrofit2.Response

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {

    // Cek apakah access token hampir kadaluarsa (misal sisa <= 5 detik)
    private fun isAccessTokenAlmostExpired(thresholdSeconds: Long = 5L): Boolean {
        val expSec = tokenManager.getAccessTokenExp() ?: return false  // kalau exp belum disimpan, anggap aman

        val nowSec = System.currentTimeMillis() / 1000
        val sisa = expSec - nowSec

        return sisa <= thresholdSeconds
    }

    // PANGGIL endpoint refresh token
    // - Kirim refresh_token lewat COOKIE: "refresh_token=<value>"
    // - Terima access_token + refresh_token baru dalam JSON body
    // - Simpan ke TokenManager
    suspend fun refreshTokens(): Boolean {
        val refreshRaw = tokenManager.getRefreshToken()
        if (refreshRaw.isNullOrEmpty()) {
            Log.e("AuthRepository", "Refresh token kosong, tidak bisa refresh")
            return false
        }

        // Kirim sebagai cookie: refresh_token=<value_yang_kamu_simpan>
        val cookieHeader = "refresh_token=$refreshRaw"
        Log.d("AuthRepository", "Kirim cookie: $cookieHeader")

        return try {
            // Pastikan ApiService.refreshToken cocok:
            // @POST("/api/auth/user/refresh")
            // suspend fun refreshToken(@Header("Cookie") cookie: String): Response<RefreshResponse>
            val res: Response<RefreshResponse> = api.refreshToken(cookieHeader)

            if (!res.isSuccessful) {
                Log.e(
                    "AuthRepository",
                    "Refresh gagal, code=${res.code()}, errorBody=${res.errorBody()?.string()}"
                )
                return false
            }

            val body = res.body()
            if (body == null) {
                Log.e("AuthRepository", "Refresh body null")
                return false
            }

            // Simpan token baru (sekalian update exp di TokenManager)
            tokenManager.saveTokens(body.accessToken, body.refreshToken)
            Log.d(
                "AuthRepository",
                "Refresh BERHASIL. access baru = ${body.accessToken.take(20)}..."
            )
            true

        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception saat refresh: ${e.message}")
            false
        }
    }

    // Bungkus panggilan API:
    // - Cek token mau habis → refresh dulu (proaktif)
    // - Kalau tetap dapat 401 → refresh sekali lagi, lalu ulang request
    suspend fun <T> callWithAutoRefresh(
        apiCall: suspend () -> Response<T>
    ): Response<T> {

        // 1. PROACTIVE: kalau mau expired → refresh dulu
        if (isAccessTokenAlmostExpired()) {
            val ok = refreshTokens()
            if (!ok) {
                Log.w("AuthRepository", "Proaktif refresh gagal, lanjut pakai token lama")
            }
        }

        // 2. Coba request pertama
        val first = apiCall()
        if (first.code() != 401) {
            return first
        }

        Log.w("AuthRepository", "Dapat 401, coba refresh lalu ulang request...")

        // 3. FALLBACK: kalau tetap 401 → coba refresh sekali lagi
        val refreshed = refreshTokens()
        if (!refreshed) {
            Log.e("AuthRepository", "Refresh gagal setelah 401, balikin response awal")
            return first
        }

        // 4. Ulang request dengan token baru
        return apiCall()
    }
}