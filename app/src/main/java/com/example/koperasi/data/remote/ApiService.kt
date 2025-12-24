package com.example.koperasi.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import okhttp3.RequestBody


data class GenericResponse(
    val message: String?
)

data class LogoutResponse(
    @SerializedName("message")
    val message: String
)

interface ApiService {

    @POST("api/auth/user/login")
    suspend fun loginGoogle(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    // ✅ register payload harus sesuai backend (lihat DTO di bawah)
    @Multipart
    @POST("/api/auth/user/register")
    suspend fun registerUserMultipart(
        @Part("id_token") idToken: RequestBody,

        @Part("name") name: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("npwp") npwp: RequestBody?,

        @Part("jenis_kelamin") jenisKelamin: RequestBody,
        @Part("agama") agama: RequestBody,
        @Part("tempat_lahir") tempatLahir: RequestBody,
        @Part("tanggal_lahir") tanggalLahir: RequestBody,
        @Part("alamat_domisili") alamatDomisili: RequestBody,
        @Part("register_location") registerLocation: RequestBody,
        @Part("register_id") registerId: RequestBody,
        @Part("pekerjaan") pekerjaan: RequestBody,
        @Part("status_perkawinan") statusPerkawinan: RequestBody,
        @Part("warga_negara") wargaNegara: RequestBody,
        @Part("no_hp") noHp: RequestBody,

        @Part ktpImage: MultipartBody.Part?,
        @Part profileImage: MultipartBody.Part?
    ): Response<GenericResponse>

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
