package com.example.koperasi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R
import com.example.koperasi.ui.theme.KoperasiTheme

data class PromoItem(
    val title: String,
    val subtitle: String,
    val iconRes: Int
)

@Composable
fun HomeScreen(
    onLogoutSuccess: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { },
            )
        }
    ) { innerPadding ->
        HomeContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            selectedTab = selectedTab,
            onLogoutSuccess = onLogoutSuccess
        )
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        NavigationBar(
            modifier = Modifier
                .shadow(8.dp, shape = shape, clip = false)
                .clip(shape),
            containerColor = Color.White,
            tonalElevation = 0.dp,
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                icon = {
                    Image(
                        painterResource(id = R.drawable.menu),
                        contentDescription = "Menu",
                        modifier = Modifier.size(24.dp))},
                label = { Text("Menu") }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                icon = {
                    Image(
                        painterResource(R.drawable.daftar),
                        contentDescription = "Daftar",
                        modifier = Modifier.size(24.dp)) },
                label = { Text("Daftar") }
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                icon = { Image(
                    painterResource(R.drawable.tanggal),
                    contentDescription = "Tanggal",
                    modifier = Modifier.size(24.dp)) },
                label = { Text("Tanggal") }
            )
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                icon = {
                    Image(
                        painterResource(R.drawable.profil),
                        contentDescription = "Profil",
                        modifier = Modifier.size(24.dp)) },
                label = { Text("Profil") }
            )
        }
    }
}


@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    onLogoutSuccess: () -> Unit
) {
    Box(modifier = modifier.background(Color(0xFFF5F5F5))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // HEADER ORANYE + CARD PUTIH
            DashboardHeader()

            // Spacer buat card putih yang di-offset ke bawah
            Spacer(Modifier.height(100.dp))

            // ====== ⬇️ PROMO ORANGE LAZYROW DI SINI ⬇️ ======
            val promoItems = listOf(
                PromoItem(
                    title = "Pelatihan & Perizinan",
                    subtitle = "Kredit Usaha Rakyat",
                    iconRes = R.drawable.splash
                ),
                PromoItem(
                    title = "Akses Permodalan",
                    subtitle = "(Keuangan)",
                    iconRes = R.drawable.splash
                ),
                PromoItem(
                    title = "Akses Permodalan",
                    subtitle = "(Keuangan)",
                    iconRes = R.drawable.splash
                )
                // kalau mau nambah promo lagi, tambahin di sini
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promoItems) { item ->
                    PromoOrangeCard(item = item)
                }
            }
            // ====== ⬆️ AKHIR PROMO ORANGE LAZYROW ⬆️ ======

            Spacer(Modifier.height(16.dp))

            // PROGRAM PELATIHAN TERBARU
            SectionTitle("Program Pelatihan Terbaru")
            PlaceholderCard(height = 120.dp)

            Spacer(Modifier.height(8.dp))

            // EVENT & KEGIATAN KOPERASI
            SectionTitle("Event & Kegiatan Koperasi Gerai")
            PlaceholderCard(height = 140.dp)

            Spacer(Modifier.height(8.dp))

            // UMKM NEWS
            SectionTitle("UMKM NEWS")
            PlaceholderCard(height = 140.dp)

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onLogoutSuccess,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)
            ) {
                Text("Logout")
            }

            Spacer(Modifier.height(80.dp))
        }

        // FAB chat kamu tetap
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-20).dp)
                .size(60.dp),
            shape = CircleShape,
            color = Color(0xFFF68E1E),
            shadowElevation = 4.dp
        ) {
            IconButton(onClick = { /* TODO: implement */ }) {
                Image(
                    painter = painterResource(R.drawable.message),
                    contentDescription = "Chat",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun PromoOrangeCard(
    item: PromoItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(160.dp)
            .height(123.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF9B317),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // --- Gambar pusat ---
            Box(
                modifier = Modifier
                    .width(142.dp)
                    .height(69.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFD9D9D9)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.kur),
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(80.dp)                      // jangan fillMaxSize biar gak ketarik aneh
                )
            }

            Spacer(Modifier.height(2.dp))

            // --- Judul ---
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}




@Composable
private fun DashboardHeader() {
    val orange = Color(0xFFF68E1E)
    val shape = RoundedCornerShape(
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, shape = shape, clip = false)
            .background(orange, shape = shape)
            .height(324.dp)
    ) {
        // ===== KONTEN ATAS: avatar, nama, saldo, logo kanan =====
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kiri: avatar + nama
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar bulat placeholder
                    Image(
                        painter = painterResource(id = R.drawable.placeholder_profile),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(50.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Suparjo",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Kanan: logo koperasi (placeholder)

                Image(
                    painter = painterResource(id = R.drawable.logo1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(75.dp)
                )

            }

            Spacer(Modifier.height(12.dp))

            // Saldo
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "Rp 5.000",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.visible),
                    contentDescription = "Coin",
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Row menu Scan / Top Up / Send / Request
            Row(
                modifier = Modifier
                    .width(304.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderActionItem(
                    label = "Scan",
                    iconRes = R.drawable.scan
                )
                HeaderActionItem(
                    label = "Top Up",
                    iconRes = R.drawable.topup
                )
                HeaderActionItem(
                    label = "Send",
                    iconRes = R.drawable.send
                )
                HeaderActionItem(
                    label = "Request",
                    iconRes = R.drawable.request
                )
            }
        }

        // ===== CARD PUTIH DI BAWAH YANG OVERLAP =====
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .offset(y = 80.dp), //  keluar dari oranye
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(304.dp)
                    .height(174.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.width(204.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppShortcutItem(
                        label = "Kimo",
                        iconRes = R.drawable.kimo
                    )
                    AppShortcutItem(
                        label = "Bachra",
                        iconRes = R.drawable.kambing
                    )
                    AppShortcutItem(
                        label = "Burindo",
                        iconRes = R.drawable.burindo
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.width(204.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppShortcutItem(
                        label = "PLN",
                        iconRes = R.drawable.pln
                    )
                    AppShortcutItem(
                        label = "Koperasi",
                        iconRes = R.drawable.gerai
                    )
                    AppShortcutItem(
                        label = "More",
                        iconRes = R.drawable.more
                    )
                }
            }
        }
    }
}


// Icon + label kecil (Scan / Top Up / dll)
@Composable
private fun HeaderActionItem(
    label: String,
    iconRes: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

// Shortcut app di card putih (Kimo, Bachra, dst.)
@Composable
private fun AppShortcutItem(
    label: String,
    iconRes: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(48.dp)
        )

        Spacer(Modifier.height(0.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}



@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun PlaceholderCard(
    height: Dp
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .height(height)
            .background(Color(0xFFDDDDDD), shape = RoundedCornerShape(16.dp))
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HomeScreenPreview() {
    KoperasiTheme {
        HomeScreen(
            onLogoutSuccess = { /* no-op in preview */ }
        )
    }
}
