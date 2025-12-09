package com.example.koperasi.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class GenericResponse(
    val message: String?
)

data class LogoutResponse(
    @SerializedName("message")
    val message: String
)
interface ApiService {

    // POST login
    @POST("api/auth/user/login")
    suspend fun loginGoogle(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    // POST register
    @POST("api/auth/user/register")
    suspend fun registerGoogle(
        @Body body: RegisterRequest
    ): Response<GenericResponse>

    // POST logout (dengan JWT di Authorization header)
    @POST("/api/auth/user/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String
    ): Response<LogoutResponse>

    @POST("/api/auth/user/refresh")
    suspend fun refreshToken(
        @Header("Cookie") cookie: String
    ): Response<RefreshResponse>

    @GET("/api/auth/user/me")
    suspend fun getMe(
        @Header("Authorization") bearer: String
    ): Response<UserData>
}