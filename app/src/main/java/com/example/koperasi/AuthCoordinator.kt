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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class AuthCoordinator(
    private val context: Context,
    private val googleAuth: GoogleAuthUiClient,
    private val credentialManager: CredentialManager,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val getNavController: () -> NavHostController?
) {

    private var isRegisterFlow = false
    private var locationValue: String = "UNKNOWN_LOCATION"

    private val WEB_CLIENT_ID =
        "1085008448604-0oucanl872c1lkrovvsptl9k9jts7hsd.apps.googleusercontent.com"

    fun startGoogleSignIn(
        isRegisterFlow: Boolean,
        location: String
    ) {
        this.isRegisterFlow = isRegisterFlow
        this.locationValue = location

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 🔥 WAJIB CLEAR CACHE
                credentialManager.clearCredentialState(
                    ClearCredentialStateRequest()
                )

                val option = GetGoogleIdOption.Builder()
                    .setServerClientId(WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false) // 🔥 WAJIB
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)

                handleGoogleToken(credential.idToken ?: return@launch)

            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH", e.message ?: "")
            }
        }
    }

    private fun handleGoogleToken(googleToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseUser = googleAuth.currentUser()
                    ?: googleAuth.signInWithToken(googleToken).user
                    ?: return@launch

                val tokenResult = firebaseUser
                    .getIdToken(true)
                    ?.await()
                    ?: return@launch

                val firebaseIdToken = tokenResult.token ?: return@launch

                tokenManager.saveIdToken(firebaseIdToken)

                if (isRegisterFlow) {
                    withContext(Dispatchers.Main) {
                        getNavController()?.navigate("complete_profile")
                    }
                    return@launch
                }

                val res = ApiClient.api.loginGoogle(
                    LoginRequest(
                        idToken = firebaseIdToken,
                        location = locationValue
                    )
                )

                if (res.isSuccessful) {
                    res.body()?.let {
                        tokenManager.saveTokens(it.accessToken, it.refreshToken)
                    }

                    withContext(Dispatchers.Main) {
                        getNavController()?.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                } else {
                    Log.e("LOGIN", res.errorBody()?.string() ?: "")
                }

            } catch (e: Exception) {
                Log.e("HANDLE_TOKEN", e.message ?: "")
            }
        }
    }


    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 🔥 HIT API LOGOUT
                authRepository.logout()

            } catch (e: Exception) {
                Log.e("LOGOUT_API", e.message ?: "")
            } finally {
                withContext(Dispatchers.Main) {
                    // 🔥 CLEAR SEMUA STATE LOKAL
                    credentialManager.clearCredentialState(
                        ClearCredentialStateRequest()
                    )
                    googleAuth.signOut()
                    tokenManager.clearTokens()

                    // 🔥 BALIK KE LOGIN
                    getNavController()?.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

}

