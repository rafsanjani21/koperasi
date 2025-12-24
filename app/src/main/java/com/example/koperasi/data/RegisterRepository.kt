package com.example.koperasi.data

import android.content.Context
import android.util.Log
import com.example.koperasi.TokenManager
import com.example.koperasi.data.remote.ApiService
import com.example.koperasi.data.remote.LoginRequest
import com.example.koperasi.pages.CompleteProfileForm
import com.example.koperasi.utils.DeviceInfo
import com.example.koperasi.utils.filePart
import com.example.koperasi.utils.textPart

class RegisterRepository(
    private val api: ApiService,
    private val context: Context,
    private val tokenManager: TokenManager,
) {
    suspend fun registerThenLogin(idToken: String, form: CompleteProfileForm) {
        // 1) REGISTER (multipart)
        registerOneApiMultipart(idToken, form)

        // 2) LOGIN
        val device = DeviceInfo.getDeviceInfo()
        val deviceInfo =
            "Android ${device["os_version"]} (API ${device["api_level"]}); " +
                    "Brand=${device["device_brand"]}; Model=${device["device_model"]}"

        val loginRes = api.loginGoogle(LoginRequest(idToken, deviceInfo))
        if (!loginRes.isSuccessful) {
            val err = loginRes.errorBody()?.string()
            throw IllegalStateException("Login gagal: ${loginRes.code()} ${err ?: loginRes.message()}")
        }

        val body = loginRes.body()
        if (body == null) throw IllegalStateException("Login gagal: body kosong")

        // 3) SAVE TOKENS
        tokenManager.saveTokens(body.accessToken, body.refreshToken)
        Log.d("REGISTER_LOGIN", "Login success -> tokens saved")
    }
    suspend fun registerOneApiMultipart(idToken: String, form: CompleteProfileForm) {

        val ktpPart = form.ktpImageUri?.let { filePart(context, it, "ktp_image") }
        val profilePart = form.profilePhotoUri?.let { filePart(context, it, "profile_image") }

        val res = api.registerUserMultipart(
            idToken = textPart(idToken),

            name = textPart(form.nama),
            nik = textPart(form.nik),
            npwp = form.npwp.takeIf { it.isNotBlank() }?.let { textPart(it) },

            jenisKelamin = textPart(genderLabelFromValue(form.jenisKelamin)),
            agama = textPart(form.agama),
            tempatLahir = textPart(form.tempatLahirKabKota),
            tanggalLahir = textPart(ddMmYyyyToIso(form.tglLahir)),
            alamatDomisili = textPart(buildAlamatDomisili(form)),
            registerLocation = textPart(form.registerLocation.ifBlank { form.kabupaten }),
            registerId = textPart(form.registerId.ifBlank { "0.0.0.0" }),
            pekerjaan = textPart(form.pekerjaan),
            statusPerkawinan = textPart(form.statusPerkawinan),
            wargaNegara = textPart(form.kewarganegaraan),
            noHp = textPart(form.noHp),

            ktpImage = ktpPart,
            profileImage = profilePart
        )

        if (!res.isSuccessful) {
            val err = res.errorBody()?.string()
            throw IllegalStateException("Register gagal: ${res.code()} ${err ?: res.message()}")
        }
    }
}

// ===== helpers mapping backend (dipakai repo) =====
// Karena ini logic “data mapping”, lebih cocok di repository (atau file util), bukan UI.

private fun genderLabelFromValue(value: String): String =
    when (value) {
        "L" -> "Laki-laki"
        "P" -> "Perempuan"
        else -> ""
    }

private fun ddMmYyyyToIso(date: String): String {
    val parts = date.split("-")
    require(parts.size == 3) { "Format tanggal harus dd-MM-yyyy, dapat: $date" }

    val dd = parts[0].padStart(2, '0')
    val mm = parts[1].padStart(2, '0')
    val yyyy = parts[2]
    return "$yyyy-$mm-$dd"
}


private fun buildAlamatDomisili(form: CompleteProfileForm): String {
    val rtRw = if (form.rt.isNotBlank() && form.rw.isNotBlank()) "RT ${form.rt}/RW ${form.rw}" else ""
    return listOf(
        form.alamat,
        rtRw,
        form.kelurahan,
        form.kecamatan,
        form.kabupaten,
        form.provinsi
    ).filter { it.isNotBlank() }.joinToString(", ")
}
