package com.example.koperasi.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * GoogleAuthUiClient
 *
 * Kelas helper untuk menangani autentikasi Google
 * menggunakan Firebase Authentication.
 *
 * Tanggung jawab utama:
 * - Login Firebase menggunakan Google ID Token
 * - Logout pengguna dari Firebase
 * - Mengambil informasi user yang sedang login
 *
 * Kelas ini berfungsi sebagai lapisan autentikasi awal
 * sebelum token dikirim ke backend aplikasi.
 *
 * @param context Context Android (disiapkan untuk kebutuhan UI/ekstensi di masa depan)
 */
class GoogleAuthUiClient(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    // Login Firebase pakai Google ID Token (dari Credential Manager)
    /**
     * signInWithToken
     *
     * Melakukan login ke Firebase menggunakan Google ID Token.
     * ID Token biasanya diperoleh dari:
     * - Credential Manager
     * - Google Sign-In API
     *
     * Jika berhasil:
     * - Firebase akan mengembalikan FirebaseUser
     *
     * Jika gagal:
     * - Exception akan dilempar dan ditangani di layer atas
     *
     * @param idToken Google ID Token hasil autentikasi
     * @return AuthResult hasil autentikasi Firebase
     */
    suspend fun signInWithToken(idToken: String) =
        auth.signInWithCredential(
            GoogleAuthProvider.getCredential(idToken, null)
        ).await()

    /**
     * signOut
     *
     * Melakukan logout dari Firebase Authentication.
     * Biasanya dipanggil saat:
     * - User logout dari aplikasi
     * - Token sudah tidak valid
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * currentUser
     *
     * Mengambil informasi user yang sedang login di Firebase.
     *
     * @return FirebaseUser jika ada, atau null jika belum login
     */
    fun currentUser() = auth.currentUser
}