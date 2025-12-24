package com.example.koperasi.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.koperasi.TokenManager
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.pages.CompleteProfileScreen
import com.example.koperasi.pages.LoginScreen
import com.example.koperasi.pages.RegisterScreen
import com.example.koperasi.pages.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    isLoggedIn: Boolean,
    tokenManager: TokenManager,
    onGoogleLogin: () -> Unit,
    onGoogleRegister: () -> Unit,
    onLogout: () -> Unit
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable("splash") { SplashScreen(navController, isLoggedIn) }

        composable("login") {
            LoginScreen(
                onNavigateRegister = { navController.navigate("register") },
                onGoogleLogin = onGoogleLogin
            )
        }

        composable("register") {
            val infoMessage =
                navController.currentBackStackEntry?.savedStateHandle?.get<String>("info") ?: ""

            RegisterScreen(
                infoMessage = infoMessage,
                onNavigateLogin = { navController.popBackStack() },
                onGoogleRegister = onGoogleRegister
            )
        }

        // Kode yang benar
        composable("complete_profile") {
            CompleteProfileScreen(
                idTokenProvider = {
                    tokenManager.getIdToken()
                        ?: throw IllegalStateException("ID Token kosong. Silakan login ulang.")
                },
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("complete_profile") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }





        composable("home") {
            val authRepo = remember { AuthRepository(ApiClient.api, tokenManager) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(5_000)
                    if (tokenManager.isAccessTokenAlmostExpired(5)) {
                        val ok = authRepo.refreshTokens()
                        Log.d("AUTO_REFRESH", "auto refresh: $ok")
                    }
                }
            }

            // ✅ semua navigasi merchant/shopping/payment ada di dalam MainBottomNavScreen
            MainBottomNavScreen(
                onLogoutSuccess = onLogout
            )
        }
    }
}

