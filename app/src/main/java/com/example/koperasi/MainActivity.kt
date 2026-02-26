package com.example.koperasi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.credentials.CredentialManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koperasi.auth.GoogleAuthUiClient
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.navigation.AppNavGraph
import com.example.koperasi.ui.theme.KoperasiTheme
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var googleAuth: GoogleAuthUiClient
    private lateinit var credentialManager: CredentialManager
    private lateinit var authCoordinator: AuthCoordinator
    private var nav: NavHostController? = null
    private val tokenManager by lazy { TokenManager(this) }
    private val authRepository by lazy { AuthRepository(ApiClient.api, tokenManager) }

    private val requestLocationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                // 🔥 Setelah permission diberikan → cek GPS
                if (!isLocationEnabled()) {
                    openLocationSettings()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 CEK GPS SAAT PERTAMA KALI
        checkLocationPermission()

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

            val isLoggedIn by tokenManager.isLoggedIn.collectAsState()

            // 🔥 AMBIL NAMA DARI TOKEN MANAGER
            val fullName = tokenManager.getUserName() ?: ""
            Log.d("DEBUG_NAME", "FULL NAME = $fullName")
            val firstName = fullName
                .trim()
                .split("\\s+".toRegex())
                .firstOrNull()
                ?: ""

            KoperasiTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavGraph(
                        navController = navController,
                        startDestination = "splash",
                        isLoggedIn = isLoggedIn,
                        tokenManager = tokenManager,
                        authCoordinator = authCoordinator,
                        userName = firstName,
                        onLogout = {
                            tokenManager.clearTokens()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {

                // Permission sudah ada → cek GPS
                if (!isLocationEnabled()) {
                    openLocationSettings()
                }
            }

            else -> {
                requestLocationPermission.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }
}