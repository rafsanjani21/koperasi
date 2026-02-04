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
import com.google.gson.Gson

// =====================
// API ERROR MODEL
// =====================
data class ApiErrorResponse(
    val error: String?,
    val message: String?
)

// =====================
// REPOSITORY
// =====================
class RegisterRepository(
    private val api: ApiService,
    private val context: Context,
    private val tokenManager: TokenManager,
) {

    // =====================
    // REGISTER → LOGIN
    // =====================
    suspend fun registerThenLogin(
        idToken: String,
        form: CompleteProfileForm
    ) {
        registerMultipart(idToken, form)

        val device = DeviceInfo.getDeviceInfo()
        val deviceInfo =
            "Android ${device["os_version"]} (API ${device["api_level"]}); " +
                    "Brand=${device["device_brand"]}; Model=${device["device_model"]}"

        val loginRes = api.loginGoogle(
            LoginRequest(idToken, deviceInfo)
        )

        if (!loginRes.isSuccessful) {
            val errBody = loginRes.errorBody()?.string().orEmpty()

            // 🔥 USER BELUM VERIFIED BUKAN ERROR FATAL
            if (loginRes.code() == 400 && errBody.contains("not verified", true)) {
                Log.w("LOGIN", "User belum diverifikasi, lanjut ke login screen")
                return
            }

            val message = parseErrorMessage(
                code = loginRes.code(),
                rawBody = errBody,
                defaultMessage = "Login gagal"
            )

            throw IllegalStateException(message)
        }

        val body = loginRes.body()
            ?: throw IllegalStateException("Login gagal: response kosong")

        tokenManager.saveTokens(
            body.accessToken,
            body.tokenHash
        )
    }

    // =====================
    // REGISTER MULTIPART
    // =====================
    private suspend fun registerMultipart(
        idToken: String,
        form: CompleteProfileForm
    ) {

        Log.d(
            "UPLOAD",
            """
            KTP URI     = ${form.ktpImageUri}
            PROFILE URI = ${form.profilePhotoUri}
            """.trimIndent()
        )

        requireNotNull(form.ktpImageUri) { "Foto KTP wajib diunggah" }
        requireNotNull(form.profilePhotoUri) { "Foto profil wajib diunggah" }

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
                    form.ktpImageUri,
                    "ktp_picture"
                ),

                profile_picture = filePart(
                    context,
                    form.profilePhotoUri,
                    "profile_picture"
                )
            )

            if (!res.isSuccessful) {
                val rawError = res.errorBody()?.string()

                Log.e(
                    "UPLOAD",
                    "Register gagal ${res.code()} | body=$rawError"
                )

                val message = parseErrorMessage(
                    code = res.code(),
                    rawBody = rawError,
                    defaultMessage = "Registrasi gagal"
                )

                throw IllegalStateException(message)
            }

        } catch (e: Exception) {
            Log.e("UPLOAD", "Multipart crash", e)
            throw e
        }
    }

    // =====================
    // ERROR PARSER
    // =====================
    private fun parseErrorMessage(
        code: Int,
        rawBody: String?,
        defaultMessage: String
    ): String {

        val apiError = try {
            Gson().fromJson(rawBody, ApiErrorResponse::class.java)
        } catch (e: Exception) {
            null
        }

        // 🔥 CUSTOM UX MAPPING
        return when {
            apiError?.error?.contains("nik", ignoreCase = true) == true ->
                "NIK sudah terdaftar. Silakan gunakan NIK lain."

            apiError?.error?.contains("phone", ignoreCase = true) == true ->
                "Nomor HP sudah digunakan."

            apiError?.message != null ->
                apiError.message

            !rawBody.isNullOrBlank() ->
                rawBody

            else ->
                "$defaultMessage ($code)"
        }
    }
}
