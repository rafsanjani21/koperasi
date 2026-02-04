package com.example.koperasi

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import org.json.JSONObject

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
    fun saveTokens(accessToken: String, tokenHash: String?) {
        if (tokenHash.isNullOrEmpty()) {
            Log.e("TOKEN", "token_hash null saat saveTokens")
            return
        }

        val expSec = parseJwtExp(accessToken)

        prefs.edit {
            putString(ACCESS_TOKEN, accessToken)
            putString(TOKEN_HASH, tokenHash)
            if (expSec != null) {
                putLong(ACCESS_TOKEN_EXP, expSec)
            }
        }
    }



    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN, null)

    fun getTokenHash(): String? =
        prefs.getString(TOKEN_HASH, null)

    // ✅ ID TOKEN (untuk register complete profile)
    fun saveIdToken(idToken: String) {
        prefs.edit { putString(ID_TOKEN, idToken) }
    }

    fun getIdToken(): String? = prefs.getString(ID_TOKEN, null)

    // Ambil exp dalam detik (unix time), atau null kalau nggak ada
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


    // Parse claim "exp" dari JWT
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
