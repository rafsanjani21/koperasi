package com.example.koperasi

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavHostController
import com.example.koperasi.auth.GoogleAuthUiClient
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.data.remote.LoginRequest
import com.example.koperasi.data.remote.RegisterRequest
import com.example.koperasi.utils.DeviceInfo
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthCoordinator(
    private val context: Context,
    private val googleAuth: GoogleAuthUiClient,
    private val credentialManager: CredentialManager,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val getNavController: () -> NavHostController?
) {

    private var lastFirebaseIdToken: String? = null
    private var isRegisterFlow: Boolean = false

    private val WEB_CLIENT_ID =
        "1085008448604-0oucanl872c1lkrovvsptl9k9jts7hsd.apps.googleusercontent.com"

    // ============================================================
    // PUBLIC API buat dipanggil dari MainActivity/NavGraph
    // ============================================================

    fun startGoogleSignIn(isRegisterFlow: Boolean) {
        this.isRegisterFlow = isRegisterFlow

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val googleOpt = GetGoogleIdOption.Builder()
                    .setServerClientId(WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOpt)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val googleData = GoogleIdTokenCredential.Companion.createFrom(result.credential.data)
                val googleToken = googleData.idToken ?: return@launch

                handleGoogleToken(googleToken)

            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH", "ERROR: ${e.message}")
            }
        }
    }

    fun sendManualNameToBackend(name: String, loginSource: String = "android") {
        val idToken = lastFirebaseIdToken ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = ApiClient.api.registerGoogle(
                    RegisterRequest(idToken, name, loginSource)
                )

                if (!res.isSuccessful) {
                    val err = res.errorBody()?.string() ?: ""

                    if (err.contains("already registered")) {
                        loginAfterRegister(idToken, loginSource)
                        return@launch
                    }

                    Log.e("REGISTER", "Err: $err")
                    return@launch
                }

                // Register sukses → login
                loginAfterRegister(idToken, loginSource)

            } catch (e: Exception) {
                Log.e("REGISTER", "ERROR: ${e.message}")
            }
        }
    }

    fun logout() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val access = tokenManager.getAccessToken()

                withContext(Dispatchers.IO) {
                    if (!access.isNullOrEmpty()) {
                        try {
                            val res = ApiClient.api.logout("Bearer $access")
                            Log.d(
                                "LOGOUT",
                                "Backend logout code=${res.code()}, success=${res.isSuccessful}"
                            )
                        } catch (e: Exception) {
                            Log.e("LOGOUT", "Gagal call API logout: ${e.message}")
                        }
                    } else {
                        Log.w("LOGOUT", "Access token kosong, skip panggil API logout")
                    }
                }

                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    Log.d("LOGOUT", "Credential state cleared.")

                    googleAuth.signOut()
                    Log.d("LOGOUT", "Signed out from Firebase.")

                    tokenManager.clearTokens()
                    Log.d("LOGOUT", "Local tokens cleared.")

                    getNavController()?.navigate("login") {
                        popUpTo(getNavController()?.graph?.startDestinationId ?: 0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                    Log.d("LOGOUT", "Logout complete, navigating to login.")
                } catch (e: Exception) {
                    Log.e("LOGOUT", "Error saat bersihin lokal: ${e.message}", e)
                }

            } catch (e: Exception) {
                Log.e("LOGOUT", "Error di logout(): ${e.message}", e)
            }
        }
    }

    // ============================================================
    // INTERNAL: handle token & login
    // ============================================================

    private fun handleGoogleToken(googleToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingUser = googleAuth.currentUser()

                val firebaseIdToken = if (existingUser != null) {
                    existingUser.getIdToken(false).await().token ?: ""
                } else {
                    val result = googleAuth.signInWithToken(googleToken)
                    val user = result.user ?: return@launch
                    user.getIdToken(true).await().token ?: ""
                }

                if (firebaseIdToken.isEmpty()) return@launch
                lastFirebaseIdToken = firebaseIdToken

                val device = DeviceInfo.getDeviceInfo()
                val deviceInfo =
                    "Android ${device["os_version"]} (API ${device["api_level"]}); " +
                            "Brand=${device["device_brand"]}; Model=${device["device_model"]}"

                val loginBody = LoginRequest(firebaseIdToken, deviceInfo)
                val loginRes = ApiClient.api.loginGoogle(loginBody)

                if (loginRes.isSuccessful) {
                    val body = loginRes.body()
                    if (body != null) {
                        tokenManager.saveTokens(body.accessToken, body.refreshToken)
                        Log.d(
                            "LOGIN",
                            "access=${body.accessToken.take(20)}..., refresh=${body.refreshToken.take(20)}..."
                        )
                    }

                    withContext(Dispatchers.Main) {
                        getNavController()?.navigate("home")
                    }
                    return@launch
                }

                val error = loginRes.errorBody()?.string() ?: ""

                if (error.contains("user not registered")) {
                    withContext(Dispatchers.Main) {
                        val nav = getNavController()
                        if (isRegisterFlow) {
                            nav?.navigate("complete_profile")
                        } else {
                            nav?.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("info", "Anda belum terdaftar. Silakan registrasi dulu.")
                            nav?.navigate("register")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("HANDLE_TOKEN", "ERR: ${e.message}")
            }
        }
    }

    private fun loginAfterRegister(idToken: String, loginSource: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val device = DeviceInfo.getDeviceInfo()
                val deviceInfo =
                    "Android ${device["os_version"]} (API ${device["api_level"]}); Brand=${device["device_brand"]}"

                val loginRes = ApiClient.api.loginGoogle(LoginRequest(idToken, deviceInfo))

                if (!loginRes.isSuccessful) return@launch

                val body = loginRes.body()
                if (body != null) {
                    tokenManager.saveTokens(body.accessToken, body.refreshToken)
                }

                withContext(Dispatchers.Main) {
                    getNavController()?.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }

            } catch (e: Exception) {
                Log.e("LOGIN_AFTER_REG", "ERROR: ${e.message}")
            }
        }
    }
}