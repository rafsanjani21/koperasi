package com.example.koperasi

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import org.json.JSONObject

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val ACCESS_TOKEN_EXP = "access_token_exp"   // waktu exp dalam detik (unix time)
    }

    // Simpan access + refresh token (dipanggil saat login)
    fun saveTokens(accessToken: String, refreshToken: String) {
        val expSec = parseJwtExp(accessToken)

        prefs.edit {
            putString(ACCESS_TOKEN, accessToken)
            putString(REFRESH_TOKEN, refreshToken)
            if (expSec != null) {
                putLong(ACCESS_TOKEN_EXP, expSec)
            } else {
                remove(ACCESS_TOKEN_EXP)
            }
        }
    }

    // Update access token SAJA (misal setelah refresh token)
    fun saveAccessToken(token: String) {
        val expSec = parseJwtExp(token)

        prefs.edit {
            putString(ACCESS_TOKEN, token)
            if (expSec != null) {
                putLong(ACCESS_TOKEN_EXP, expSec)
            } else {
                remove(ACCESS_TOKEN_EXP)
            }
        }
    }

    fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN, null)
    }

    // Ambil exp dalam detik (unix time), atau null kalau nggak ada
    fun getAccessTokenExp(): Long? {
        val stored = prefs.getLong(ACCESS_TOKEN_EXP, 0L)
        return if (stored == 0L) null else stored
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    // --------- LOGIKA CEK EXPIRED OTOMATIS ----------

    // true kalau token mau habis dalam `thresholdSeconds` (default 5 detik)
    fun isAccessTokenAlmostExpired(thresholdSeconds: Long = 5L): Boolean {
        val expSec = getAccessTokenExp() ?: return false   // kalau nggak tahu exp, anggap aja aman

        val nowSec = System.currentTimeMillis() / 1000
        val sisa = expSec - nowSec
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