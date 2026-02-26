package com.example.koperasi.data

import android.content.Context
import android.util.Log
import com.example.koperasi.TokenManager
import com.example.koperasi.data.remote.ApiService
import com.example.koperasi.pages.register.CompleteProfileForm
import com.example.koperasi.utils.filePart
import com.example.koperasi.data.mapper.*
import com.example.koperasi.utils.toTextBody
import com.google.gson.Gson

// =====================
// API ERROR MODEL
// =====================
/**
 * ApiErrorResponse
 *
 * Model representasi error response dari backend API.
 * Digunakan untuk parsing pesan error agar dapat ditampilkan
 * dengan pesan yang lebih ramah ke pengguna.
 *
 * @param error Pesan error utama dari backend
 * @param message Pesan tambahan dari backend
 */
data class ApiErrorResponse(
    val error: String?,
    val message: String?
)

// =====================
// REPOSITORY
// =====================
/**
 * RegisterRepository
 *
 * Repository yang bertanggung jawab atas proses:
 * - Registrasi pengguna (multipart/form-data)
 * - Login otomatis setelah registrasi berhasil
 * - Parsing error dari backend agar user-friendly
 *
 * Repository ini berperan sebagai penghubung antara ViewModel
 * dan API Service pada proses pendaftaran anggota koperasi.
 *
 * @param api ApiService untuk komunikasi dengan backend
 * @param context Context Android untuk kebutuhan upload file
 * @param tokenManager TokenManager untuk penyimpanan token autentikasi
 */
class RegisterRepository(
    private val api: ApiService,
    private val context: Context,
    private val tokenManager: TokenManager,
) {

    // =====================
    // REGISTER → LOGIN
    // =====================
    /**
     * registerThenLogin
     *
     * Melakukan alur:
     * 1. Registrasi pengguna menggunakan multipart request
     * 2. Login otomatis menggunakan Google ID Token
     * 3. Menyimpan access token dan token hash jika login berhasil
     *
     * Catatan:
     * - Jika user belum diverifikasi oleh backend, login dianggap
     *   bukan error fatal dan proses dihentikan dengan aman.
     *
     * @param idToken Google ID Token hasil autentikasi Google
     * @param form Data lengkap profil pengguna
     * @throws IllegalStateException Jika registrasi atau login gagal
     */
    suspend fun registerOnly(
        idToken: String,
        form: CompleteProfileForm
    ) {
        registerMultipart(idToken, form)
    }

//    suspend fun registerThenLogin(
//        idToken: String,
//        form: CompleteProfileForm
//    ) {
//        // ================= REGISTRASI =================
//        registerMultipart(idToken, form)
//
//        // ================= DEVICE INFO =================
//        val device = DeviceInfo.getDeviceInfo()
//        val deviceInfo =
//            "Android ${device["os_version"]} (API ${device["api_level"]}); " +
//                    "Brand=${device["device_brand"]}; Model=${device["device_model"]}"
//
//        // ================= LOGIN =================
//        val loginRes = api.loginGoogle(
//            LoginRequest(idToken, deviceInfo)
//        )
//
//        if (!loginRes.isSuccessful) {
//            val errBody = loginRes.errorBody()?.string().orEmpty()
//
//            // 🔥 USER BELUM VERIFIED BUKAN ERROR FATAL
//            if (loginRes.code() == 400 && errBody.contains("not verified", true)) {
//                Log.w("LOGIN", "User belum diverifikasi, lanjut ke login screen")
//                return
//            }
//
//            val message = parseErrorMessage(
//                code = loginRes.code(),
//                rawBody = errBody,
//                defaultMessage = "Login gagal"
//            )
//
//            throw IllegalStateException(message)
//        }
//
//        // ================= SIMPAN TOKEN =================
//        val body = loginRes.body()
//            ?: throw IllegalStateException("Login gagal: response kosong")
//
//        val data = body.data
//            ?: throw IllegalStateException("Login gagal: data kosong")
//
//        tokenManager.saveTokens(
//            accessToken = data.accessToken,
//            tokenHash = data.tokenHash
//        )
//
//    }

    // =====================
    // REGISTER MULTIPART
    // =====================
    /**
     * registerMultipart
     *
     * Mengirim data pendaftaran pengguna menggunakan
     * multipart/form-data, termasuk:
     * - Data identitas
     * - Data kependudukan
     * - Foto KTP
     * - Foto profil
     *
     * @param idToken Google ID Token
     * @param form Data lengkap profil pengguna
     * @throws IllegalStateException Jika registrasi gagal
     */
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

        // Validasi file wajib
        requireNotNull(form.ktpImageUri) { "Foto KTP wajib diunggah" }
        requireNotNull(form.profilePhotoUri) { "Foto profil wajib diunggah" }

        try {
            val res = api.registerUserMultipart(

                // ===== AUTH =====
                idToken = idToken.toTextBody(),

                // ===== DATA PRIBADI =====
                name = form.nama.toTextBody(),
                nik = form.nik.toTextBody(),
                npwp = form.npwp.ifBlank { "-" }.toTextBody(),

                placeOfBirth = form.tempatLahirKabKota.toTextBody(),
                birth = ddMmYyyyToIso(form.tglLahir).toTextBody(),
                gender = genderLabel(form.jenisKelamin).toTextBody(),

//                address = buildAlamat(form).toTextBody(),
                province = form.provinsi.toTextBody(),
                regency = form.kabupaten.toTextBody(),
                district = form.kecamatan.toTextBody(),
                village = form.kelurahan.toTextBody(),
                rt = form.rt.toTextBody(),
                rw = form.rw.toTextBody(),
                address = form.alamat.toTextBody(),
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

                // ===== FILE UPLOAD =====
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

            // ================= ERROR HANDLING =================
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
    /**
     * parseErrorMessage
     *
     * Mengubah error mentah dari backend menjadi
     * pesan yang lebih mudah dipahami oleh pengguna.
     *
     * @param code HTTP status code
     * @param rawBody Body error mentah dari backend
     * @param defaultMessage Pesan default jika parsing gagal
     * @return Pesan error yang siap ditampilkan ke UI
     */
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

        // CUSTOM UX MAPPING
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
