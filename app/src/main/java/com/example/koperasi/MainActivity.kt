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

/**
 * MainActivity adalah entry point utama aplikasi.
 *
 * Bertanggung jawab untuk:
 * - Inisialisasi authentication (Google Sign-In)
 * - Mengelola permission lokasi
 * - Menyediakan NavController global
 * - Menjalankan Navigation Graph berbasis Jetpack Compose
 */
class MainActivity : ComponentActivity() {

    /** Client untuk autentikasi Google (Credential API wrapper) */
    private lateinit var googleAuth: GoogleAuthUiClient

    /** CredentialManager untuk mengelola login Google */
    private lateinit var credentialManager: CredentialManager

    /** Koordinator logika autentikasi (login, logout, navigasi) */
    private lateinit var authCoordinator: AuthCoordinator

    /** NavController disimpan agar bisa diakses dari luar Compose */
    private var nav: NavHostController? = null

    /** Callback tertunda yang membutuhkan lokasi pengguna */
    private var pendingAction: ((String) -> Unit)? = null

    /** TokenManager untuk menyimpan dan mengambil JWT */
    private val tokenManager by lazy { TokenManager(this) }
    private val authRepository by lazy { AuthRepository(ApiClient.api, tokenManager) }

    /** Lokasi terakhir pengguna (default jika permission ditolak) */
    private var lastKnownLocation: String = "UNKNOWN_LOCATION"

    /**
     * Launcher permission lokasi (runtime permission).
     * Akan otomatis dipanggil saat user menerima / menolak permission.
     */
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            lifecycleScope.launch {
                lastKnownLocation = if (granted)
                    LocationHelper.getCurrentLocation(this@MainActivity)
                else
                    "UNKNOWN_LOCATION"
            }
        }


    /**
     * Mengambil lokasi pengguna sebelum menjalankan aksi tertentu.
     *
     * @param action callback yang membutuhkan data lokasi
     */
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

    /**
     * Lifecycle utama Activity.
     * Seluruh dependency global dan Compose UI diinisialisasi di sini.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Google Auth Client
        googleAuth = GoogleAuthUiClient(this)
        // Inisialisasi Credential Manager
        credentialManager = CredentialManager.create(this)

        // Inisialisasi AuthCoordinator (pusat kontrol auth + navigasi)
        authCoordinator = AuthCoordinator(
            context = this,
            googleAuth = googleAuth,
            credentialManager = credentialManager,
            tokenManager = tokenManager,
            authRepository = authRepository,
            getNavController = { nav }
        )

        // Setup UI berbasis Jetpack Compose
        setContent {
            // NavController utama aplikasi
            val navController = rememberNavController()
            nav = navController

            // Cek status login dari token
            val isLoggedIn = tokenManager.getAccessToken() != null

            KoperasiTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavGraph(
                        navController = navController,
                        startDestination = "splash",
                        isLoggedIn = tokenManager.getAccessToken() != null,
                        tokenManager = tokenManager,
                        authCoordinator = authCoordinator,
                        lastKnownLocation = "JAKARTA",       // ✅ bisa dummy dulu
                        onLogout = {
                            authCoordinator.logout()
                        }
                    )
                }
            }
        }

    }
}
