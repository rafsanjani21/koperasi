package com.example.koperasi

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import org.json.JSONObject

/**
 * TokenManager
 *
 * Kelas utilitas untuk mengelola token autentikasi pengguna,
 * meliputi:
 * - Access Token (JWT)
 * - Token Hash (refresh / server-side token)
 * - ID Token (Google / Firebase)
 *
 * Token disimpan secara lokal menggunakan SharedPreferences.
 * Selain penyimpanan, kelas ini juga menangani:
 * - Parsing expiry JWT
 * - Pengecekan token hampir kedaluwarsa
 * - Penghapusan token saat logout
 *
 * @param context Context Android untuk mengakses SharedPreferences
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val TOKEN_HASH = "token_hash"
        private const val ACCESS_TOKEN_EXP = "access_token_exp"   // waktu exp dalam detik (unix time)

        // ✅ Tambahan: simpan id_token (firebase/google id token)
        private const val ID_TOKEN = "id_token"
    }

    // Simpan access + refresh token (dipanggil saat login)
    /**
     * saveTokens
     *
     * Menyimpan access token dan token hash ke local storage.
     * Expiry access token diambil dari claim "exp" pada JWT.
     *
     * @param accessToken JWT access token dari backend
     * @param tokenHash Token hash untuk kebutuhan logout / invalidasi
     */
    fun saveTokens(accessToken: String, tokenHash: String?) {
        // Validasi token hash
        if (tokenHash.isNullOrEmpty()) {
            Log.e("TOKEN", "token_hash null saat saveTokens")
            return
        }

        // Ambil expiry token dari JWT
        val expSec = parseJwtExp(accessToken)

        prefs.edit {
            putString(ACCESS_TOKEN, accessToken)
            putString(TOKEN_HASH, tokenHash)
            if (expSec != null) {
                putLong(ACCESS_TOKEN_EXP, expSec)
            }
        }
    }


    /**
     * getAccessToken
     *
     * @return Access token JWT atau null jika belum login
     */
    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN, null)

    /**
     * getTokenHash
     *
     * @return Token hash atau null jika tidak tersedia
     */
    fun getTokenHash(): String? =
        prefs.getString(TOKEN_HASH, null)

    // ✅ ID TOKEN (untuk register complete profile)
    /**
     * saveIdToken
     *
     * Menyimpan Google / Firebase ID Token.
     * Digunakan pada proses registrasi lanjutan (complete profile).
     *
     * @param idToken Google / Firebase ID Token
     */
    fun saveIdToken(idToken: String) {
        prefs.edit { putString(ID_TOKEN, idToken) }
    }

    /**
     * getIdToken
     *
     * @return Google / Firebase ID Token atau null
     */
    fun getIdToken(): String? = prefs.getString(ID_TOKEN, null)

    // Ambil exp dalam detik (unix time), atau null kalau nggak ada
    /**
     * getAccessTokenExp
     *
     * Mengambil waktu kedaluwarsa access token (unix time dalam detik).
     *
     * @return Waktu exp token atau null jika tidak tersedia
     */
    fun getAccessTokenExp(): Long? {
        val stored = prefs.getLong(ACCESS_TOKEN_EXP, 0L)
        return if (stored == 0L) null else stored
    }

    fun clearTokens() {
        // kalau kamu mau clear semuanya
        prefs.edit().clear().apply()
    }

    // --------- LOGIKA CEK EXPIRED OTOMATIS ----------

    // true kalau token mau habis dalam `thresholdSeconds` (default 5 detik)
    fun isAccessTokenAlmostExpired(thresholdSeconds: Long = 5L): Boolean {
        val expSec = getAccessTokenExp()

        Log.d("TOKEN_DEBUG", "expSec = $expSec")

        if (expSec == null) return false

        val nowSec = System.currentTimeMillis() / 1000
        val sisa = expSec - nowSec

        Log.d("TOKEN_DEBUG", "sisa detik token = $sisa")

        return sisa <= thresholdSeconds
    }


    /**
     * parseJwtExp
     *
     * Mengekstrak claim "exp" dari JWT access token.
     *
     * @param token JWT access token
     * @return Waktu exp dalam detik (unix time) atau null jika parsing gagal
     */
    private fun parseJwtExp(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null

            var payload = parts[1]

            // Tambah padding biar base64 decode nggak error
            val padding = (4 - payload.length % 4) % 4
            payload += "=".repeat(padding)

            val decodedBytes = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val json = JSONObject(String(decodedBytes, Charsets.UTF_8))
            val exp = json.optLong("exp", 0L)
            if (exp == 0L) null else exp
        } catch (_: Exception) {
            null
        }
    }
}
