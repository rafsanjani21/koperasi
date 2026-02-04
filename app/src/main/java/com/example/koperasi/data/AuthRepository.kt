package com.example.koperasi.data

import com.example.koperasi.TokenManager
import com.example.koperasi.data.remote.*
import org.json.JSONObject

class AuthRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {

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
            val errorBody = res.errorBody()?.string()
            val message = try {
                JSONObject(errorBody ?: "{}")
                    .optString("error", "Login gagal")
            } catch (e: Exception) {
                "Login gagal"
            }

            throw Exception(message)
        }

        val body = res.body()!!
        tokenManager.saveTokens(body.accessToken, body.tokenHash)
    }

    suspend fun logout() {
        val accessToken = tokenManager.getAccessToken() ?: return
        val tokenHash = tokenManager.getTokenHash() ?: return

        api.logout(
            bearer = "Bearer $accessToken",
            body = LogoutRequest(tokenHash)
        )
    }
}
