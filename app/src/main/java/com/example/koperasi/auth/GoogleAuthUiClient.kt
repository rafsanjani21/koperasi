package com.example.koperasi.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()

    // Login Firebase pakai Google ID Token (dari Credential Manager)
    suspend fun signInWithToken(idToken: String) =
        auth.signInWithCredential(
            GoogleAuthProvider.getCredential(idToken, null)
        ).await()

    fun signOut() {
        auth.signOut()
    }

    fun currentUser() = auth.currentUser
}