package com.example.koperasi

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val TOKEN_HASH = "token_hash"
        private const val ACCESS_TOKEN_EXP = "access_token_exp"
        private const val ID_TOKEN = "id_token"
    }

    // 🔥 AUTH STATE (INI KUNCI UTAMA)
    private val _isLoggedIn = MutableStateFlow(getAccessToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    // ================= SAVE =================

    fun saveTokens(accessToken: String, tokenHash: String?) {
        val expSec = parseJwtExp(accessToken)

        prefs.edit {
            putString(ACCESS_TOKEN, accessToken)
            if (!tokenHash.isNullOrEmpty()) {
                putString(TOKEN_HASH, tokenHash)
            }
            if (expSec != null) {
                putLong(ACCESS_TOKEN_EXP, expSec)
            }
        }

        _isLoggedIn.value = true
        Log.d("TOKEN", "Tokens saved, loggedIn = true")
    }

    fun saveIdToken(idToken: String) {
        prefs.edit { putString(ID_TOKEN, idToken) }
    }

    // ================= GET =================

    fun getAccessToken(): String? =
        prefs.getString(ACCESS_TOKEN, null)

    fun getTokenHash(): String? =
        prefs.getString(TOKEN_HASH, null)

    fun getIdToken(): String? =
        prefs.getString(ID_TOKEN, null)

    fun getAccessTokenExp(): Long? {
        val stored = prefs.getLong(ACCESS_TOKEN_EXP, 0L)
        return if (stored == 0L) null else stored
    }

    // ================= CLEAR =================

    fun clearTokens() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        Log.d("TOKEN", "Tokens cleared, loggedIn = false")
    }

    // ================= EXPIRED =================

    fun isAccessTokenAlmostExpired(thresholdSeconds: Long = 5L): Boolean {
        val expSec = getAccessTokenExp() ?: return false
        val nowSec = System.currentTimeMillis() / 1000
        return (expSec - nowSec) <= thresholdSeconds
    }

    // ================= JWT =================

    private fun parseJwtExp(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null

            var payload = parts[1]
            val padding = (4 - payload.length % 4) % 4
            payload += "=".repeat(padding)

            val decodedBytes = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )

            val json = JSONObject(String(decodedBytes, Charsets.UTF_8))
            val exp = json.optLong("exp", 0L)
            if (exp == 0L) null else exp
        } catch (e: Exception) {
            Log.e("TOKEN", "parseJwtExp error", e)
            null
        }
    }
}
