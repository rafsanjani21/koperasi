package com.example.koperasi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koperasi.auth.GoogleAuthUiClient
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.navigation.AppNavGraph
import com.example.koperasi.ui.theme.KoperasiTheme
import com.example.koperasi.utils.LocationHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var googleAuth: GoogleAuthUiClient
    private lateinit var credentialManager: CredentialManager
    private lateinit var authCoordinator: AuthCoordinator

    private var nav: NavHostController? = null
    private var pendingAction: ((String) -> Unit)? = null

    private val tokenManager by lazy { TokenManager(this) }
    private val authRepository by lazy { AuthRepository(ApiClient.api, tokenManager) }

    private var lastKnownLocation: String = "UNKNOWN_LOCATION"


    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            lifecycleScope.launch {
                lastKnownLocation = if (granted)
                    LocationHelper.getCurrentLocation(this@MainActivity)
                else
                    "UNKNOWN_LOCATION"
            }
        }


    private fun getLocationThen(action: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            lifecycleScope.launch {
                lastKnownLocation =
                    LocationHelper.getCurrentLocation(this@MainActivity)
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

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

            // ✅ FIX: definisi isLoggedIn
            val isLoggedIn = tokenManager.getAccessToken() != null

            KoperasiTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavGraph(
                        navController = navController,
                        startDestination = "splash",
                        isLoggedIn = isLoggedIn,
                        tokenManager = tokenManager,
                        onGoogleLogin = {
                            authCoordinator.startGoogleSignIn(
                                isRegisterFlow = false,
                                location = lastKnownLocation
                            )
                        }

                        ,
                        onGoogleRegister = {
                            authCoordinator.startGoogleSignIn(
                                isRegisterFlow = true,
                                location = lastKnownLocation
                            )
                        }
                        ,
                        onLogout = { authCoordinator.logout() }
                    )
                }
            }
        }

    }
}
