package com.example.koperasi.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.koperasi.TokenManager
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.pages.CompleteProfileScreen
import com.example.koperasi.pages.HomeScreen
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
    authRepository: AuthRepository,
    onGoogleLogin: () -> Unit,
    onGoogleRegister: () -> Unit,
    onManualNameSubmitted: (String) -> Unit,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // SPLASH
        composable("splash") {
            SplashScreen(
                navController = navController,
                isLoggedIn = isLoggedIn
            )
        }

        // LOGIN
        composable("login") {
            LoginScreen(
                onNavigateRegister = { navController.navigate("register") },
                onGoogleLogin = onGoogleLogin
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
                onGoogleRegister = onGoogleRegister
            )
        }

        // COMPLETE PROFILE
        composable("complete_profile") {
            CompleteProfileScreen(
                onSubmit = { name ->
                    onManualNameSubmitted(name)
                }
            )
        }

        // HOME
        composable("home") {
            val scope = rememberCoroutineScope()
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

            HomeScreen(
                onLogoutSuccess = { onLogout() }
            )
        }
    }
}
