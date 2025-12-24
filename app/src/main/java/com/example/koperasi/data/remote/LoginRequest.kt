package com.example.koperasi.data.remote

import com.google.gson.annotations.SerializedName

// =============== REQUESTS ===============

data class LoginRequest(
    @SerializedName("id_token")
    val idToken: String,

    @SerializedName("device_info")
    val deviceInfo: String
)

// =============== RESPONSES ===============

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: UserData
)

data class RefreshResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)


data class UserData(
    @SerializedName("ID")
    val id: Int,

    @SerializedName("GoogleUID")
    val googleUid: String,

    @SerializedName("Name")
    val name: String,

    @SerializedName("Email")
    val email: String,

    @SerializedName("GooglePicture")
    val googlePicture: String?,

    @SerializedName("ProfilePicture")
    val profilePicture: String?,

    @SerializedName("Role")
    val role: String?,

    @SerializedName("IsLoggedIn")
    val isLoggedIn: Int
)