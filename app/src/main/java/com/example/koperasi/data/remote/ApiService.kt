package com.example.koperasi.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class GenericResponse(
    val message: String?
)

data class LogoutResponse(
    @SerializedName("message")
    val message: String
)

data class RefreshRequest(
    @SerializedName("token_hash")
    val tokenHash: String
)


interface ApiService {

    // ================= LOGIN =================
    @POST("/api/auth/user/login")
    suspend fun loginGoogle(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    // ================= REGISTER =================
    @Multipart
    @POST("/api/auth/user/register")
    suspend fun registerUserMultipart(

        // 🔥 INI WAJIB SESUAI BACKEND
        @Part("id_token") idToken: RequestBody,

        @Part("name") name: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("npwp") npwp: RequestBody,

        @Part("place_of_birth") placeOfBirth: RequestBody,
        @Part("birth") birth: RequestBody,
        @Part("gender") gender: RequestBody,

        @Part("address") address: RequestBody,
        @Part("pos_code") posCode: RequestBody,

        @Part("religion") religion: RequestBody,
        @Part("marital_status") maritalStatus: RequestBody,

        @Part("job") job: RequestBody,
        @Part("citizenship") citizenship: RequestBody,
        @Part("blood_type") bloodType: RequestBody,
        @Part("phone_number") phoneNumber: RequestBody,
        @Part("register_location") registerLocation: RequestBody,

        @Part("last_education") lastEducation: RequestBody,
        @Part("active_as") activeAs: RequestBody,
        @Part("mother_name") motherName: RequestBody,

        @Part ktp_picture: MultipartBody.Part,
        @Part profile_picture: MultipartBody.Part
    ): Response<GenericResponse>



    // ================= LOGOUT =================
    @POST("/api/auth/user/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String,
        @Body body: LogoutRequest
    ): Response<LogoutResponse>



    // ================= REFRESH =================
    @POST("/api/auth/user/refresh")
    suspend fun refreshToken(
        @Body body: RefreshRequest
    ): Response<RefreshResponse>

}
