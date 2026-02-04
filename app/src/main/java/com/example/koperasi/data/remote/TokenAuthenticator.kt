package com.example.koperasi.data.remote

import com.example.koperasi.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val apiService: ApiService
) : Authenticator {

    @Synchronized
    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {

        if (responseCount(response) >= 2) {
            tokenManager.clearTokens()
            return null
        }

        val tokenHash = tokenManager.getTokenHash() ?: return null

        val refreshResponse = runBlocking {
            apiService.refreshToken(
                RefreshRequest(tokenHash)
            )
        }

        if (!refreshResponse.isSuccessful) {
            tokenManager.clearTokens()
            return null
        }

        val body = refreshResponse.body() ?: return null

        tokenManager.saveTokens(
            body.accessToken,
            body.refreshToken
        )

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${body.accessToken}")
            .build()
    }

    private fun responseCount(response: okhttp3.Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
