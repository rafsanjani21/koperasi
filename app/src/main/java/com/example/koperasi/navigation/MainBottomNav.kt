package com.example.koperasi.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.koperasi.R
import com.example.koperasi.pages.geraidigital.GeraiDigitalScreen
import com.example.koperasi.pages.geraimart.GeraiMartScreen
import com.example.koperasi.pages.main.HomeScreen
import com.example.koperasi.pages.main.UserProfileScreen
import com.example.koperasi.pages.shop.MerchantProductScreen
import com.example.koperasi.pages.shop.PaymentMethodScreen
import com.example.koperasi.pages.shop.ShoppingListScreen

/* =========================
   BOTTOM NAV ITEM
========================= */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int
) {
    data object Menu : BottomNavItem("home", "Menu", R.drawable.menu)
    data object Transaksi : BottomNavItem("transaksi", "Transaksi", R.drawable.daftar)
    data object Promo : BottomNavItem("promo", "Promo", R.drawable.promo)
    data object Profil : BottomNavItem("profil", "Profil", R.drawable.profil)
}

/* =========================
   MAIN BOTTOM NAV
========================= */
@Composable
fun MainBottomNavScreen(
    onLogoutSuccess: () -> Unit,
    userName: String
) {
    val navController = rememberNavController()
    val cartState = remember { CartState() }

    val bottomNavItems = listOf(
        BottomNavItem.Menu,
        BottomNavItem.Transaksi,
        BottomNavItem.Promo,
        BottomNavItem.Profil
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar =
        currentRoute?.startsWith("merchant") != true &&
                currentRoute?.startsWith("geraimart") != true &&
                currentRoute != "shopping_list" &&
                currentRoute != "payment_method"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    items = bottomNavItems,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Menu.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            /* ===== BOTTOM NAV SCREENS ===== */
            composable(BottomNavItem.Menu.route) {
                HomeScreen(
                    userName = userName,
                    onLogout = onLogoutSuccess,
                    onOpenGeraiMart = {
                        navController.navigate("geraimart")
                    },
                    onOpenGeraiDigital = {
                        navController.navigate("geraidigital")
                    }
                )
            }

            composable(BottomNavItem.Transaksi.route) {
                SimplePage("Halaman Transaksi")
            }

            composable(BottomNavItem.Promo.route) {
                SimplePage("Halaman Promo")
            }

            composable(BottomNavItem.Profil.route) {
                UserProfileScreen()
            }

            /* ===== MERCHANT ===== */
            composable(
                route = "merchant/{merchantId}",
                arguments = listOf(
                    navArgument("merchantId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val merchantId =
                    backStackEntry.arguments?.getString("merchantId").orEmpty()

                MerchantProductScreen(
                    merchantId = merchantId,
                    onBackClick = { navController.popBackStack() },
                    onAddToCart = { product ->
                        val id = "${merchantId}_${product.name}"
                        cartState.addOrIncrement(
                            id,
                            product.name,
                            product.price,
                            product.imageRes
                        )
                    },
                    onOpenShoppingList = {
                        navController.navigate("shopping_list")
                    },
                    navController = navController
                )
            }

            /* ===== SHOPPING LIST ===== */
            composable("shopping_list") {
                ShoppingListScreen(
                    cartState = cartState,
                    onBackClick = { navController.popBackStack() },
                    onPayClick = {
                        navController.navigate("payment_method")
                    }
                )
            }

            /* ===== GERAI MART ===== */
            composable("geraimart") {
                GeraiMartScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddToCart = { product ->
                        val id = "geraimart_${product.name}"
                        cartState.addOrIncrement(
                            id,
                            product.name,
                            product.price,
                            product.imageRes
                        )
                    },
                    onOpenShoppingList = {
                        navController.navigate("shopping_list")
                    },
                    navController = navController
                )
            }

            /* ===== GERAI DIGITAL ===== */
            composable("geraidigital") {
                GeraiDigitalScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            /* ===== PAYMENT ===== */
            composable("payment_method") {
                PaymentMethodScreen(
                    onPay = { /* TODO */ },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

/* =========================
   BOTTOM NAV BAR
========================= */
@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedColor = Color(0xFF4461AD)
    val unselectedColor = Color(0xFF9E9E9E)

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label,
                        tint = if (selected) selectedColor else unselectedColor
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected) selectedColor else unselectedColor
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = selectedColor.copy(alpha = 0.12f)
                )
            )
        }
    }
}

/* =========================
   SIMPLE PAGE
========================= */
@Composable
private fun SimplePage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}
