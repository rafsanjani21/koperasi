package com.example.koperasi

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavHostController
import com.example.koperasi.auth.GoogleAuthUiClient
import com.example.koperasi.data.AuthRepository
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
    private var locationValue = "UNKNOWN"

    private val WEB_CLIENT_ID =
        "1085008448604-0oucanl872c1lkrovvsptl9k9jts7hsd.apps.googleusercontent.com"

    fun startGoogleSignIn(
        isRegisterFlow: Boolean,
        location: String,
        onError: (String) -> Unit
    ) {
        this.isRegisterFlow = isRegisterFlow
        this.locationValue = location

        CoroutineScope(Dispatchers.Main).launch {
            try {
                credentialManager.clearCredentialState(
                    ClearCredentialStateRequest()
                )

                val option = GetGoogleIdOption.Builder()
                    .setServerClientId(WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)

                handleGoogleToken(credential.idToken!!, onError)

            } catch (e: Exception) {
                onError("Login Google dibatalkan")
            }
        }
    }

    private fun handleGoogleToken(
        googleToken: String,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseUser = googleAuth.currentUser()
                    ?: googleAuth.signInWithToken(googleToken).user
                    ?: return@launch

                val firebaseIdToken =
                    firebaseUser.getIdToken(true).await().token ?: return@launch

                tokenManager.saveIdToken(firebaseIdToken)

                if (isRegisterFlow) {
                    withContext(Dispatchers.Main) {
                        getNavController()?.navigate("complete_profile")
                    }
                    return@launch
                }

                authRepository.loginGoogle(
                    idToken = firebaseIdToken,
                    location = locationValue
                )

                withContext(Dispatchers.Main) {
                    getNavController()?.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }

            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("not verified", true) == true ->
                        "Akun Anda belum diverifikasi oleh admin.\nSilakan menunggu pihak koperasi."
                    else -> e.message ?: "Login gagal"
                }

                Log.e("LOGIN_ERROR", message)

                withContext(Dispatchers.Main) {
                    onError(message)
                }
            }
        }
    }

    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                authRepository.logout()
            } finally {
                withContext(Dispatchers.Main) {
                    credentialManager.clearCredentialState(
                        ClearCredentialStateRequest()
                    )
                    googleAuth.signOut()
                    tokenManager.clearTokens()

                    getNavController()?.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}
