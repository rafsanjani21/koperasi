package com.example.koperasi.data

import android.util.Log
import com.example.koperasi.TokenManager
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.data.remote.ApiService
import com.example.koperasi.data.remote.LoginRequest
import com.example.koperasi.data.remote.LogoutRequest
import com.example.koperasi.data.remote.RefreshRequest
import com.example.koperasi.data.remote.RefreshResponse
import retrofit2.Response

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun logout(): Boolean {
        return try {
            val accessToken = tokenManager.getAccessToken() ?: return false
            val refreshToken = tokenManager.getRefreshToken() ?: return false

            val res = api.logout(
                bearer = "Bearer $accessToken",
                body = LogoutRequest(
                    tokenHash = refreshToken // 🔥 INI PENTING
                )
            )

            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }


    suspend fun refreshTokens(): Boolean {
        return try {
            val refreshToken = tokenManager.getRefreshToken() ?: return false

            val res = api.refreshToken(
                RefreshRequest(
                    tokenHash = refreshToken // 🔥
                )
            )

            if (res.isSuccessful) {
                res.body()?.let {
                    tokenManager.saveTokens(
                        it.accessToken,
                        it.refreshToken
                    )
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }




    suspend fun loginGoogle(
        idToken: String,
        location: String
    ) {
        val res = api.loginGoogle(
            LoginRequest(
                idToken = idToken,
                location = location
            )
        )

        if (!res.isSuccessful) {
            throw IllegalStateException(
                res.errorBody()?.string() ?: "Login gagal"
            )
        }

        val body = res.body()!!
        tokenManager.saveTokens(body.accessToken, body.refreshToken)
    }


}