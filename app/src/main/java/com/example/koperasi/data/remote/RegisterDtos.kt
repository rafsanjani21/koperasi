package com.example.koperasi.data.remote

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("id_token")
    val idToken: String,

    @SerializedName("user")
    val user: RegisterUserDto
)

data class RegisterUserDto(
    @SerializedName("name") val name: String,
    @SerializedName("nik") val nik: String,
    @SerializedName("npwp") val npwp: String?, // boleh null
    @SerializedName("jenis_kelamin") val jenisKelamin: String,
    @SerializedName("agama") val agama: String,
    @SerializedName("tempat_lahir") val tempatLahir: String,
    @SerializedName("tanggal_lahir") val tanggalLahir: String, // yyyy-MM-dd
    @SerializedName("alamat_domisili") val alamatDomisili: String,
    @SerializedName("register_location") val registerLocation: String,
    @SerializedName("register_id") val registerId: String,
    @SerializedName("pekerjaan") val pekerjaan: String,
    @SerializedName("status_perkawinan") val statusPerkawinan: String,
    @SerializedName("warga_negara") val wargaNegara: String,
    @SerializedName("no_hp") val noHp: String,
    @SerializedName("ktp_image_path") val ktpImagePath: String
)
