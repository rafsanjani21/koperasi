package com.example.koperasi.pages

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.koperasi.R
import com.example.koperasi.ui.theme.KoperasiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Box(
                modifier = modifier.padding(6.dp)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "User",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF68E1E)
                        )
                    },
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    modifier = modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                        .clickable(onClick = { /* Handle back action */ })
                )
            }
        },
        bottomBar = { NavigationBottomBar(modifier = modifier) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Surface(
            modifier = modifier.padding(innerPadding)
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp)
            ){
                Image(
                    painter = painterResource(id = R.drawable.placeholder_profile),
                    contentDescription = "Profile",
                    modifier = modifier.size(140.dp)
                )
                Text(
                    text = "John Doe",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileButton(
                        buttonName = "Edit Profile",
                        onClick = {/**/}
                    )
                    ProfileButton(
                        buttonName = "My Activity",
                        onClick = {/**/}
                    )
                    ProfileButton(
                        buttonName = "Promos",
                        onClick = {/**/}
                    )
                    ProfileButton(
                        buttonName = "Change Language",
                        onClick = {/**/}
                    )
                    ProfileButton(
                        buttonName = "Privacy Policy",
                        onClick = {/**/}
                    )
                    ProfileButton(
                        buttonName = "Terms of Service",
                        onClick = {/**/}
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileButton(
    buttonName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        colors = ButtonDefaults.buttonColors(Color(0xFFE2E2E2)),
        modifier = modifier
            .height(52.dp)
            .width(280.dp),
        onClick = {/**/}
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = buttonName,
                color = Color(0XFF171717),
                fontSize = 15.sp,
                modifier = modifier.align(Alignment.CenterStart)
            )
        }
    }
}

data class BottomBarItem(
    @DrawableRes val icon: Int,
    val name: String,
    val route: String
)

@Composable
fun NavigationBottomBar(
    modifier: Modifier = Modifier
) {
//    val navController = rememberNavController()

    // For testing only
    var selectedItem by remember { mutableIntStateOf(3) }

    NavigationBar(
        modifier = modifier
    ){
        val listBottomNavigation = listOf(
            BottomBarItem(
                icon = R.drawable.home_icon,
                name = "Beranda",
                route = "Beranda"
            ),
            BottomBarItem(
                icon = R.drawable.daftar_icon,
                name = "Daftar",
                route = "Daftar"
            ),
            BottomBarItem(
                icon = R.drawable.tanggal_icon,
                name = "Tanggal",
                route = "Tanggal"
            ),
            BottomBarItem(
                icon = R.drawable.akun_icon,
                name = "Akun",
                route = "Akun"
            )
        )

//        listBottomNavigation.map {
//            NavigationBarItem(
//                selected = it.name == navController.currentDestination?.route, // This needs to be updated with proper route comparison
//                onClick = { navController.navigate(it.name) },
//                icon = {
//                    Icon(
//                        painterResource(it.icon),
//                        contentDescription = it.name,
//                        modifier = modifier.size(24.dp)
//                    )},
//                label = {
//                    Text(
//                        text = it.name,
//                        fontSize = 11.sp,
//                        fontWeight = FontWeight.Medium
//                    ) },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Color(0xFFF68E1E)
//                ),
//            )
//        }

        // For testing only
        listBottomNavigation.forEachIndexed { index, item  ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painterResource(item.icon),
                        contentDescription = item.name,
                        modifier = modifier.size(24.dp)
                    )},
                label = {
                    when(selectedItem == index){
                        true -> {
                            Text(
                                text = item.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF68E1E)
                            )
                        }
                        false -> {
                            Text(
                                text = item.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF171717)
                            )
                        }
                    }

                },
                selected = selectedItem == index,
                onClick = { selectedItem = index },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF68E1E)
                ),
            )
        }
    }
}

@Preview
@Composable
private fun NavigationBottomBarPreview() {
    KoperasiTheme {
        NavigationBottomBar()
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileScreenPreview() {
    KoperasiTheme() {
        UserProfileScreen()
    }
}