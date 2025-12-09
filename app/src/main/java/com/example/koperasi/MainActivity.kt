package com.example.koperasi

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.koperasi.auth.GoogleAuthUiClient
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.UserRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.data.remote.LoginRequest
import com.example.koperasi.data.remote.RegisterRequest
import com.example.koperasi.pages.CompleteProfileScreen
import com.example.koperasi.pages.HomeScreen
import com.example.koperasi.pages.LoginScreen
import com.example.koperasi.pages.RegisterScreen
import com.example.koperasi.ui.theme.KoperasiTheme
import com.example.koperasi.utils.DeviceInfo
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var googleAuth: GoogleAuthUiClient
    private lateinit var credentialManager: CredentialManager

    private var nav: NavHostController? = null

    private var lastFirebaseIdToken: String? = null
    private var isRegisterFlow = false

    private val WEB_CLIENT_ID =
        "1085008448604-0oucanl872c1lkrovvsptl9k9jts7hsd.apps.googleusercontent.com"

    // prefs + tokenManager: kasih tipe eksplisit
    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("auth_prefs", MODE_PRIVATE)
    }

    // MainActivity.kt
    private val tokenManager: TokenManager by lazy {
        TokenManager(this) // Pass the activity context
    }
    private val authRepository: AuthRepository by lazy { AuthRepository(ApiClient.api, tokenManager) }
    private val userRepository: UserRepository by lazy { UserRepository(ApiClient.api, authRepository, tokenManager) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAuth = GoogleAuthUiClient(this)
        credentialManager = CredentialManager.create(this)

        // 👇 Cek token sekali sebelum compose
        val initialDestination = if (tokenManager.getAccessToken() != null) {
            "home"
        } else {
            "login"
        }

        setContent {
            val navController = rememberNavController()
            nav = navController

            KoperasiTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    NavHost(navController, startDestination = initialDestination) {

                        // LOGIN
                        composable("login") {
                            LoginScreen(
                                onNavigateRegister = { navController.navigate("register") },
                                onGoogleLogin = {
                                    isRegisterFlow = false
                                    startGoogleSignIn()
                                }
                            )
                        }

                        // REGISTER
                        composable("register") {
                            val infoMessage =
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.get<String>("info") ?: ""

                            RegisterScreen(
                                infoMessage = infoMessage,
                                onNavigateLogin = { navController.popBackStack() },
                                onGoogleRegister = {
                                    isRegisterFlow = true
                                    startGoogleSignIn()
                                }
                            )
                        }

                        // COMPLETE PROFILE
                        composable("complete_profile") {
                            CompleteProfileScreen(
                                onSubmit = { name ->
                                    sendManualNameToBackend(name)
                                }
                            )
                        }

                        // HOME
                        composable("home") {
                            val scope = rememberCoroutineScope()
                            val authRepo = remember { AuthRepository(ApiClient.api, tokenManager) }

                            // auto refresh loop yang tadi
                            LaunchedEffect(Unit) {
                                while (true) {
                                    kotlinx.coroutines.delay(5_000)
                                    if (tokenManager.isAccessTokenAlmostExpired(5)) {
                                        val ok = authRepo.refreshTokens()
                                        Log.d("AUTO_REFRESH", "auto refresh: $ok")
                                    }
                                }
                            }

                            HomeScreen(
                                onLogoutSuccess = { logout() }
                            )
                        }
                    }
                }
            }
        }
    }


    // ============================================================
    // GOOGLE SIGN-IN
    // ============================================================
    private fun startGoogleSignIn() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val googleOpt = GetGoogleIdOption.Builder()
                    .setServerClientId(WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOpt)
                    .build()

                val result = credentialManager.getCredential(this@MainActivity, request)
                val googleData = GoogleIdTokenCredential.createFrom(result.credential.data)
                val googleToken = googleData.idToken ?: return@launch

                handleGoogleToken(googleToken)

            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH", "ERROR: ${e.message}")
            }
        }
    }

    // ============================================================
    // HANDLE GOOGLE TOKEN
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

                // LOGIN BACKEND
                val loginBody = LoginRequest(firebaseIdToken, deviceInfo)
                val loginRes = ApiClient.api.loginGoogle(loginBody)

                if (loginRes.isSuccessful) {
                    val body = loginRes.body()
                    if (body != null) {
                        tokenManager.saveTokens(body.accessToken, body.refreshToken)
                        Log.d("LOGIN", "access=${body.accessToken.take(20)}..., refresh=${body.refreshToken.take(20)}...")
                    }

                    runOnUiThread {
                        nav?.navigate("home")
                    }
                    return@launch
                }

                val error = loginRes.errorBody()?.string() ?: ""

                if (error.contains("user not registered")) {
                    if (isRegisterFlow) {
                        runOnUiThread { nav?.navigate("complete_profile") }
                    } else {
                        runOnUiThread {
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

    // ============================================================
    // REGISTER GOOGLE → PAKAI NAMA MANUAL
    // ============================================================
    private fun sendManualNameToBackend(name: String, loginSource: String = "android") {
        val idToken = lastFirebaseIdToken ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = ApiClient.api.registerGoogle(
                    RegisterRequest(idToken, name, loginSource)
                )

                // Bila user sudah ada → tetap login
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

    // ============================================================
    // AFTER REGISTER → LOGIN
    // ============================================================
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

                runOnUiThread {
                    nav?.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }

            } catch (e: Exception) {
                Log.e("LOGIN_AFTER_REG", "ERROR: ${e.message}")
            }
        }
    }


    // ============================================================
    // LOGOUT
    // ============================================================
    // Tambahkan fungsi ini di dalam class MainActivity
    private fun logout() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val access = tokenManager.getAccessToken()

                // 1. Panggil API logout di thread IO
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

                // 2. Bersihin sisi lokal (masih di coroutine Main)
                try {
                    // a. Credential Manager
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    Log.d("LOGOUT", "Credential state cleared.")

                    // b. Firebase Auth
                    googleAuth.signOut()
                    Log.d("LOGOUT", "Signed out from Firebase.")

                    // c. SharedPreferences (token)
                    tokenManager.clearTokens()
                    Log.d("LOGOUT", "Local tokens cleared.")

                    // d. Navigate ke login dan bersihkan backstack
                    nav?.navigate("login") {
                        popUpTo(nav?.graph?.startDestinationId ?: 0) {
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

}