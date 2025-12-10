package com.example.koperasi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.credentials.CredentialManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koperasi.auth.GoogleAuthUiClient
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.UserRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.navigation.AppNavGraph
import com.example.koperasi.ui.theme.KoperasiTheme

class MainActivity : ComponentActivity() {

    private lateinit var googleAuth: GoogleAuthUiClient
    private lateinit var credentialManager: CredentialManager

    private var nav: NavHostController? = null

    private val tokenManager: TokenManager by lazy { TokenManager(this) }
    private val authRepository: AuthRepository by lazy { AuthRepository(ApiClient.api, tokenManager) }
    private val userRepository: UserRepository by lazy { UserRepository(ApiClient.api, authRepository, tokenManager) }

    private lateinit var authCoordinator: AuthCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAuth = GoogleAuthUiClient(this)
        credentialManager = CredentialManager.create(this)

        authCoordinator = AuthCoordinator(
            context = this,
            googleAuth = googleAuth,
            credentialManager = credentialManager,
            tokenManager = tokenManager,
            authRepository = authRepository,
            getNavController = { nav }
        )

        setContent {
            val navController = rememberNavController()
            nav = navController

            val isLoggedIn = tokenManager.getAccessToken() != null

            KoperasiTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavGraph(
                        navController = navController,
                        startDestination = "splash",
                        isLoggedIn = isLoggedIn,
                        tokenManager = tokenManager,
                        authRepository = authRepository,
                        onGoogleLogin = { authCoordinator.startGoogleSignIn(isRegisterFlow = false) },
                        onGoogleRegister = { authCoordinator.startGoogleSignIn(isRegisterFlow = true) },
                        onManualNameSubmitted = { name -> authCoordinator.sendManualNameToBackend(name) },
                        onLogout = { authCoordinator.logout() }
                    )
                }
            }
        }
    }
}
