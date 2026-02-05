package com.example.koperasi.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.koperasi.AuthCoordinator
import com.example.koperasi.TokenManager
import com.example.koperasi.data.AuthRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.pages.*
import com.example.koperasi.viewmodel.AuthViewModel
import com.example.koperasi.viewmodel.AuthViewModelFactory

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    isLoggedIn: Boolean,
    tokenManager: TokenManager,
    authCoordinator: AuthCoordinator,
    lastKnownLocation: String,
    onLogout: () -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ================= SPLASH =================
        composable("splash") {
            SplashScreen(navController, isLoggedIn)
        }

        // ================= LOGIN =================
        composable("login") {

            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(
                    AuthRepository(ApiClient.api, tokenManager)
                )
            )


            LoginScreen(
                viewModel = authViewModel,
                onNavigateRegister = {
                    navController.navigate("register")
                },
                onLoginGoogle = {
                    authCoordinator.startGoogleSignIn(
                        isRegisterFlow = false,
                        location = lastKnownLocation,
                        onError = { msg: String ->   // ✅ FIX TYPE
                            authViewModel.setError(msg)
                        }
                    )
                }
            )
        }

        // ================= REGISTER =================
        composable("register") {

            val infoMessage =
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>("info")
                    ?: ""

            RegisterScreen(
                infoMessage = infoMessage,
                onNavigateLogin = {
                    navController.popBackStack()
                },
                onGoogleRegister = {
                    authCoordinator.startGoogleSignIn(
                        isRegisterFlow = true,
                        location = lastKnownLocation,
                        onError = { msg: String ->
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("info", msg)
                        }
                    )
                }
            )
        }

        // ================= COMPLETE PROFILE =================
        composable("complete_profile") {

            val idToken = tokenManager.getIdToken()

            if (idToken == null) {
                navController.navigate("login") {
                    popUpTo("complete_profile") { inclusive = true }
                }
            } else {
                CompleteProfileScreen(
                    idTokenProvider = { idToken },
                    onSuccess = {
                        navController.navigate("login") {
                            popUpTo("complete_profile") { inclusive = true }
                        }
                    }
                )
            }
        }

        // ================= HOME =================
        composable("home") {

            MainBottomNavScreen(
                onLogoutSuccess = onLogout
            )
        }
    }
}
