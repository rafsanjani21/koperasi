package com.example.koperasi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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

data class PromoItem(
    val title: String,
    val subtitle: String,
    val iconRes: Int
)

@Composable
fun HomePageContent(
    onLogoutSuccess: () -> Unit,
    onOpenMerchant: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // ruang bottom bar + FAB
        ) {
            item {
                DashboardHeader(onOpenMerchant = onOpenMerchant)
            }

            item { Spacer(Modifier.height(100.dp)) }

            item {
                val promoItems = listOf(
                    PromoItem("Pelatihan & Perizinan", "Kredit Usaha Rakyat", R.drawable.splash),
                    PromoItem("Akses Permodalan", "(Keuangan)", R.drawable.splash),
                    PromoItem("Akses Permodalan", "(Keuangan)", R.drawable.splash),
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(promoItems) { item ->
                        PromoOrangeCard(item = item)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item { SectionTitle("Program Pelatihan Terbaru") }
            item { PlaceholderCard(height = 120.dp) }

            item { Spacer(Modifier.height(8.dp)) }

            item { SectionTitle("Event & Kegiatan Koperasi Gerai") }
            item { PlaceholderCard(height = 140.dp) }

            item { Spacer(Modifier.height(8.dp)) }

            item { SectionTitle("UMKM NEWS") }
            item { PlaceholderCard(height = 140.dp) }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                OutlinedButton(
                    onClick = onLogoutSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Logout")
                }
            }
        }

        // FAB chat tetap overlay
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-20).dp)
                .size(60.dp),
            shape = CircleShape,
            color = Color(0xFFF68E1E),
            shadowElevation = 4.dp
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Image(
                    painter = painterResource(R.drawable.cs),
                    contentDescription = "Chat",
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

// ===== PROMO CARD =====
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
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(2.dp))

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

// ===== HEADER & LAIN2 =====
@Composable
private fun DashboardHeader(
    onOpenMerchant: (String) -> Unit   // <-- terima lambda
) {
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
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                Image(
                    painter = painterResource(id = R.drawable.logo1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(75.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            var isBalanceVisible by remember { mutableStateOf(true) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = if (isBalanceVisible) "Rp 5.000" else "Rp ••••••",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = { isBalanceVisible = !isBalanceVisible },
                    modifier = Modifier.size(32.dp)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isBalanceVisible) R.drawable.visible else R.drawable.visibility
                        ),
                        contentDescription = if (isBalanceVisible) "Sembunyikan saldo" else "Tampilkan saldo",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }


            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .width(304.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderActionItem("Scan", R.drawable.scan)
                HeaderActionItem("Top Up", R.drawable.topup)
                HeaderActionItem("Send", R.drawable.send)
                HeaderActionItem("Request", R.drawable.request)
            }
        }

        // card putih dengan shortcut merchant
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .offset(y = 80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    AppShortcutItem("Kimo", R.drawable.kimo) {
                        onOpenMerchant("kimo")
                    }
                    AppShortcutItem("Bachra", R.drawable.kambing) {
                        onOpenMerchant("bachra")
                    }
                    AppShortcutItem("Burindo", R.drawable.burindo) {
                        onOpenMerchant("burindo")
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.width(204.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppShortcutItem("PLN", R.drawable.pln) {
                        onOpenMerchant("pln")
                    }
                    AppShortcutItem("Koperasi", R.drawable.gerai) {
                        onOpenMerchant("gerai")
                    }
                    AppShortcutItem("More", R.drawable.more) {
                        onOpenMerchant("more")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderActionItem(label: String, iconRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

@Composable
private fun AppShortcutItem(
    label: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F5F5),
            onClick = onClick
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier
                    .size(48.dp)
                    .padding(0.dp)
            )
        }

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
private fun PlaceholderCard(height: Dp) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .height(height)
            .background(Color(0xFFDDDDDD), shape = RoundedCornerShape(16.dp))
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomePagePreview() {
    HomePageContent(
        onLogoutSuccess = { },
        onOpenMerchant = { }   // <-- dummy lambda untuk preview
    )
}
