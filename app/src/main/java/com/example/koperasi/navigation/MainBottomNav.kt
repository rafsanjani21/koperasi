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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.koperasi.R
import com.example.koperasi.pages.HomePageContent

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int
) {
    data object Menu : BottomNavItem("home_menu", "Menu", R.drawable.menu)
    data object Daftar : BottomNavItem("daftar", "Daftar", R.drawable.daftar)
    data object Tanggal : BottomNavItem("tanggal", "Tanggal", R.drawable.tanggal)
    data object Profil : BottomNavItem("profil", "Profil", R.drawable.profil)
}

@Composable
fun MainBottomNavScreen(
    onLogoutSuccess: () -> Unit,
    onOpenMerchant: (String) -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Menu,
        BottomNavItem.Daftar,
        BottomNavItem.Tanggal,
        BottomNavItem.Profil
    )

    Scaffold(
        bottomBar = {
            BottomNavBar(
                items = items,
                navController = navController
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Menu.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Menu.route) {
                HomePageContent(
                    onLogoutSuccess = onLogoutSuccess,
                    onOpenMerchant = onOpenMerchant)
            }
            composable(BottomNavItem.Daftar.route) {
                SimplePage("Halaman Daftar")
            }
            composable(BottomNavItem.Tanggal.route) {
                SimplePage("Halaman Tanggal")
            }
            composable(BottomNavItem.Profil.route) {
                SimplePage("Halaman Profil")
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    navController: NavHostController
) {
    val shape = RoundedCornerShape(24.dp)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedColor = Color(0xFFF68E1E)   // orange
    val unselectedColor = Color(0xFF9E9E9E) // abu-abu

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            modifier = Modifier
                .shadow(8.dp, shape = shape, clip = false)
                .clip(shape),
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


