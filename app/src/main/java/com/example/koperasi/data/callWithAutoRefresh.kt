package com.example.koperasi.data

import com.example.koperasi.TokenManager
import com.example.koperasi.data.remote.ApiService
import com.example.koperasi.data.remote.UserData
import retrofit2.Response

class UserRepository(
    private val api: ApiService,
    private val authRepo: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend fun getMe(): Response<UserData> {
        return authRepo.callWithAutoRefresh {
            val access = tokenManager.getAccessToken() ?: ""
            api.getMe("Bearer $access")
        }
    }
}