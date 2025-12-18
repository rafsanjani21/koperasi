package com.example.koperasi.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.koperasi.TokenManager
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.pages.CompleteProfileScreen
import com.example.koperasi.pages.LoginScreen
import com.example.koperasi.pages.RegisterScreen
import com.example.koperasi.pages.SplashScreen
import kotlinx.coroutines.delay
import com.example.koperasi.pages.ProductItem


// ⬇️ IMPORT INI TAMBAHKAN
import com.example.koperasi.navigation.MainBottomNavScreen
import com.example.koperasi.pages.MerchantProductScreen
import com.example.koperasi.pages.ShoppingListScreen

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

        composable("complete_profile") {
            CompleteProfileScreen(onSubmit = onManualNameSubmitted)
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

