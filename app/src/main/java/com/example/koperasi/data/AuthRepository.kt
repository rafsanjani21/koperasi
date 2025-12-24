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

}