package com.example.koperasi.data.remote

import com.google.gson.annotations.SerializedName

// =============== REQUESTS ===============

data class LoginRequest(
    @SerializedName("id_token")
    val idToken: String,

    @SerializedName("location")
    val location: String
)

// =============== RESPONSES ===============

//data class LoginResponse(
//    @SerializedName("access_token")
//    val accessToken: String,
//
//    @SerializedName("token_hash")
//    val tokenHash: String,
//
//    @SerializedName("message")
//    val message: String,
//
//    @SerializedName("user")
//    val user: UserData
//)

data class LoginData(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_hash")
    val tokenHash: String,

    @SerializedName("user")
    val user: UserData
)

data class LoginResponse(
    @SerializedName("error")
    val error: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: LoginData?
)


data class RefreshResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_hash")
    val tokenHash: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)


//data class UserData(
//    @SerializedName("ID")
//    val id: Int,
//
//    @SerializedName("GoogleUID")
//    val googleUid: String,
//
//    @SerializedName("Name")
//    val name: String,
//
//    @SerializedName("Email")
//    val email: String,
//
//    @SerializedName("GooglePicture")
//    val googlePicture: String?,
//
//    @SerializedName("ProfilePicture")
//    val profilePicture: String?,
//
//    @SerializedName("Role")
//    val role: String?,
//
//    @SerializedName("IsLoggedIn")
//    val isLoggedIn: Int
//)

data class UserData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String?
)



data class LogoutRequest(
    @SerializedName("token_hash")
    val tokenHash: String
)
