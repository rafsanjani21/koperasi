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

/**
 * AuthCoordinator bertindak sebagai orkestrator alur autentikasi Google.
 *
 * Tanggung jawab utama:
 * - Mengelola Google Sign-In menggunakan Credential Manager
 * - Sinkronisasi login Firebase + backend
 * - Menentukan alur login vs registrasi
 * - Mengatur navigasi setelah autentikasi
 * - Menangani logout secara menyeluruh
 *
 * Class ini memisahkan logic kompleks autentikasi
 * dari UI (Compose) dan ViewModel agar tetap clean.
 */
class AuthCoordinator(
    private val context: Context,
    private val googleAuth: GoogleAuthUiClient,
    private val credentialManager: CredentialManager,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val getNavController: () -> NavHostController?
) {

    /** Flag untuk menentukan apakah proses ini login atau registrasi */
    private var isRegisterFlow = false

    /** Informasi lokasi / device yang dikirim ke backend saat login */
    private var locationValue = "UNKNOWN"

    /** Web Client ID Google OAuth (sesuai Firebase Console) */
    private val WEB_CLIENT_ID =
        "1085008448604-0oucanl872c1lkrovvsptl9k9jts7hsd.apps.googleusercontent.com"

    /* =========================================================
     * GOOGLE SIGN-IN ENTRY POINT
     * ========================================================= */

    /**
     * Memulai proses Google Sign-In menggunakan Credential Manager.
     *
     * Alur:
     * 1. Clear credential state (hindari auto login akun lama)
     * 2. Tampilkan akun Google ke user
     * 3. Ambil Google ID Token
     * 4. Lanjutkan ke proses Firebase + backend
     *
     * @param isRegisterFlow true jika dipanggil dari proses registrasi
     * @param location Informasi lokasi / device
     * @param onError Callback jika proses gagal
     */
    fun startGoogleSignIn(
        isRegisterFlow: Boolean,
        onError: (String) -> Unit
    ) {
        this.isRegisterFlow = isRegisterFlow
//        this.locationValue = location

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Bersihkan credential sebelumnya
                credentialManager.clearCredentialState(
                    ClearCredentialStateRequest()
                )
                googleAuth.signOut()

                // Konfigurasi Google ID Token
                val option = GetGoogleIdOption.Builder()
                    .setServerClientId(WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()

                // Request credential ke sistem
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()

                // Ambil credential dari Google
                val result = credentialManager.getCredential(context, request)
                val credential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)

                // Lanjutkan ke proses token
                handleGoogleToken(credential.idToken!!, onError)

            } catch (e: Exception) {
                // Biasanya terjadi jika user membatalkan dialog
                onError("Login Google dibatalkan")
            }
        }
    }

    /* =========================================================
     * TOKEN HANDLING
     * ========================================================= */

    /**
     * Mengelola Google ID Token:
     * - Login ke Firebase
     * - Ambil Firebase ID Token
     * - Simpan token
     * - Tentukan navigasi login / register
     */
    private fun handleGoogleToken(
        googleToken: String,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Login / ambil user Firebase
                val firebaseUser =
                    googleAuth.signInWithToken(googleToken).user
                        ?: throw Exception("Firebase login gagal")

                // Ambil Firebase ID Token (JWT)
                val firebaseIdToken =
                    firebaseUser.getIdToken(true).await().token ?: return@launch

                // Simpan ID token untuk proses register lengkap
                tokenManager.saveIdToken(firebaseIdToken)

                // ===== ALUR REGISTER =====
                if (isRegisterFlow) {
                    withContext(Dispatchers.Main) {
                        getNavController()?.navigate("complete_profile")
                    }
                    return@launch
                }

                // ===== ALUR LOGIN =====
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
                // Mapping pesan error agar lebih ramah user
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

    /* =========================================================
     * LOGOUT
     * ========================================================= */

    /**
     * Logout user secara menyeluruh:
     * - Logout backend
     * - Clear credential Google
     * - Logout Firebase
     * - Hapus token lokal
     * - Reset navigation ke login
     */
    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                authRepository.logout()
            }

            withContext(Dispatchers.Main) {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                googleAuth.signOut()
                tokenManager.clearTokens()
            }
        }
    }
}
