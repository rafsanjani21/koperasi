package com.example.koperasi.data

import android.content.Context
import android.util.Log
import com.example.koperasi.TokenManager
import com.example.koperasi.data.remote.ApiService
import com.example.koperasi.data.remote.LoginRequest
import com.example.koperasi.pages.CompleteProfileForm
import com.example.koperasi.utils.DeviceInfo
import com.example.koperasi.utils.filePart
import com.example.koperasi.data.mapper.*
import com.example.koperasi.utils.toTextBody


class RegisterRepository(
    private val api: ApiService,
    private val context: Context,
    private val tokenManager: TokenManager,
) {

    suspend fun registerThenLogin(idToken: String, form: CompleteProfileForm) {
        registerMultipart(idToken, form)

        val device = DeviceInfo.getDeviceInfo()
        val deviceInfo =
            "Android ${device["os_version"]} (API ${device["api_level"]}); " +
                    "Brand=${device["device_brand"]}; Model=${device["device_model"]}"

        val loginRes = api.loginGoogle(LoginRequest(idToken, deviceInfo))

        if (!loginRes.isSuccessful) {
            val err = loginRes.errorBody()?.string().orEmpty()

            // 🔥 NOT VERIFIED BUKAN ERROR FATAL
            if (loginRes.code() == 400 && err.contains("not verified", true)) {
                Log.w("LOGIN", "User belum diverifikasi, lanjut ke login screen")
                return // ⬅️ PENTING: JANGAN THROW
            }

            throw IllegalStateException("Login gagal ${loginRes.code()} $err")
        }


        val body = loginRes.body() ?: error("Login body kosong")
        tokenManager.saveTokens(body.accessToken, body.refreshToken)
    }

    private suspend fun registerMultipart(
        idToken: String,
        form: CompleteProfileForm
    ) {

        // ================= DEBUG WAJIB =================
        Log.d(
            "UPLOAD",
            """
        KTP URI     = ${form.ktpImageUri}
        PROFILE URI = ${form.profilePhotoUri}
        """.trimIndent()
        )

        // ================= VALIDASI WAJIB =================
        requireNotNull(form.ktpImageUri) { "KTP image belum dipilih" }
        requireNotNull(form.profilePhotoUri) { "Profile image belum dipilih" }

        try {
            val res = api.registerUserMultipart(

                idToken = idToken.toTextBody(),

                name = form.nama.toTextBody(),
                nik = form.nik.toTextBody(),
                npwp = form.npwp.ifBlank { "-" }.toTextBody(),

                placeOfBirth = form.tempatLahirKabKota.toTextBody(),
                birth = ddMmYyyyToIso(form.tglLahir).toTextBody(),
                gender = genderLabel(form.jenisKelamin).toTextBody(),

                address = buildAlamat(form).toTextBody(),
                posCode = form.kodePos.ifBlank { "0" }.toTextBody(),

                religion = form.agama.toTextBody(),
                maritalStatus = form.statusPerkawinan.toTextBody(),

                job = form.pekerjaan.toTextBody(),
                citizenship = form.kewarganegaraan.toTextBody(),
                bloodType = form.bloodType.ifBlank { "-" }.toTextBody(),

                phoneNumber = form.noHp.toTextBody(),
                registerLocation = form.registerLocation.toTextBody(),

                lastEducation = form.lastEducation.ifBlank { "-" }.toTextBody(),
                activeAs = form.activeAs.toTextBody(),
                motherName = form.motherName.ifBlank { "-" }.toTextBody(),

                ktp_picture = filePart(
                    context,
                    form.ktpImageUri!!,
                    "ktp_picture"
                ),

                profile_picture = filePart(
                    context,
                    form.profilePhotoUri!!,
                    "profile_picture"
                )
            )

            if (!res.isSuccessful) {
                val errorBody = res.errorBody()?.string()

                Log.e(
                    "UPLOAD",
                    "Register gagal ${res.code()} | body=$errorBody"
                )

                throw IllegalStateException("Register gagal ${res.code()}")
            }


        } catch (e: Exception) {
            Log.e("UPLOAD", "Multipart crash", e)
            throw e
        }
    }

}
