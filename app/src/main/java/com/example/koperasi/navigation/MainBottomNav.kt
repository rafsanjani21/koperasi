package com.example.koperasi.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.koperasi.R
import com.example.koperasi.pages.HomeScreen
import com.example.koperasi.pages.MerchantProductScreen
import com.example.koperasi.pages.UserProfileScreen
import com.example.koperasi.pages.PaymentMethodScreen
import com.example.koperasi.pages.ShoppingListScreen
import com.example.koperasi.pages.geraidigital.GeraiDigitalScreen
import com.example.koperasi.pages.geraimart.GeraiMartScreen


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

@Composable
fun MainBottomNavScreen(
    onLogoutSuccess: () -> Unit
) {
    val navController = rememberNavController()
    val cartState = remember { CartState() }

    val items = listOf(
        BottomNavItem.Menu,
        BottomNavItem.Transaksi,
        BottomNavItem.Promo,
        BottomNavItem.Profil
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute?.startsWith("merchant") != true &&
            currentRoute?.startsWith("geraimart") != true &&
            currentRoute != "shopping_list" &&
            currentRoute != "payment_method"

    Scaffold(
        bottomBar = { if (showBottomBar) BottomNavBar(items = items, navController = navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Menu.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(BottomNavItem.Transaksi.route) { SimplePage("Halaman Daftar") }
            composable(BottomNavItem.Promo.route) { SimplePage("Halaman Tanggal") }
            composable(BottomNavItem.Profil.route) { UserProfileScreen() }

            composable(BottomNavItem.Menu.route) {
                HomeScreen(
                    onLogoutSuccess = onLogoutSuccess,
                    navController = navController
                )
            }

            composable(
                route = "merchant/{merchantId}",
                arguments = listOf(navArgument("merchantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val merchantId = backStackEntry.arguments?.getString("merchantId").orEmpty()

                MerchantProductScreen(
                    merchantId = merchantId,
                    onBackClick = { navController.popBackStack() },
                    onAddToCart = { p ->
                        val id = "${merchantId}_${p.name}"
                        cartState.addOrIncrement(id, p.name, p.price, p.imageRes)
                    },
                    onOpenShoppingList = { navController.navigate("shopping_list") },
                    navController = navController
                )
            }

            composable("shopping_list") {
                ShoppingListScreen(
                    cartState = cartState,
                    onBackClick = { navController.popBackStack() },
                    onPayClick = { navController.navigate("payment_method") }
                )
            }

            composable("geraimart") {
                GeraiMartScreen(
                    onBackClick = { navController.navigate("home"){ popUpTo(0) } },
                    onAddToCart = { p ->
                        val id = "geraimart_${p.name}"
                        cartState.addOrIncrement(id, p.name, p.price, p.imageRes)
                    },
                    onOpenShoppingList = { navController.navigate("shopping_list") },
                    navController = navController
                )
            }

            composable("geraidigital") {
                GeraiDigitalScreen(
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable("payment_method") {
                PaymentMethodScreen(
                    onPay = { method ->
                        // TODO proses bayar
                    },
                    onBackClick = { navController.popBackStack() } // ✅ INI yang kemarin kurang
                )
            }
        }
    }
}



@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedColor = Color(0xFF4461AD)   // orange
    val unselectedColor = Color(0xFF9E9E9E) // abu-abu

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
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
                            painter = androidx.compose.ui.res.painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (selected) selectedColor else unselectedColor
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            color = if (selected) selectedColor else unselectedColor
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        // warna background “pill” di belakang icon saat selected
                        indicatorColor = selectedColor.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}


@Composable
private fun SimplePage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}


