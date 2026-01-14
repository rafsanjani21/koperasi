package com.example.koperasi.pages.geraidigital

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R

/* =========================
   DATA MODEL
========================= */
data class MenuItem(
    val title: String,
    val iconRes: Int
)

/* =========================
   DATA MENU
========================= */
val geraiMenus = listOf(
    MenuItem("Token\nListrik", R.drawable.listrik),
    MenuItem("PDAM", R.drawable.pdam),
    MenuItem("Pulsa &\nData", R.drawable.pulsa),
    MenuItem("Bayar\nBPJS", R.drawable.bpjs),
    MenuItem("Transfer", R.drawable.transfer),
    MenuItem("Point\nBelanja", R.drawable.point),
    MenuItem("Donasi", R.drawable.donasi),
    MenuItem("Riwayat\nTransaksi", R.drawable.riwayat)
)

/* =========================
   MAIN SCREEN
========================= */
@Composable
fun GeraiDigitalScreen(
    onBackClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        item {
            Box {
                HeaderGeraiDigital(onBackClick)

                MenuDigitalCard(
                    menus = geraiMenus,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp)
                        .padding(top = 120.dp)
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            Text(
                text = "Gerai Digital",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF4461AD)
            )
        }

        item { GeraiDigitalBanner() }
        item { BestSeller() }
    }
}

/* =========================
   HEADER
========================= */
@Composable
fun HeaderGeraiDigital(
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.header_digital),
            contentDescription = "geraidigitalbanner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            Text(
                text = "Gerai Digital",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp)) // balance kanan
        }
    }
}

/* =========================
   MENU CARD
========================= */
@Composable
fun MenuDigitalCard(
    menus: List<MenuItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(304   .dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            menus.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { item ->
                        MenuDigitalItem(item)
                    }
                }
            }
        }
    }
}

/* =========================
   MENU ITEM
========================= */
@Composable
fun MenuDigitalItem(item: MenuItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable { }
    ) {
        Image(
            painter = painterResource(item.iconRes),
            contentDescription = item.title,
            modifier = Modifier.size(36.dp)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}

/* =========================
   BANNER
========================= */
@Composable
fun GeraiDigitalBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
            .background(Color.LightGray, RoundedCornerShape(12.dp))
    )
}

/* =========================
   BEST SELLER
========================= */
@Composable
fun BestSeller() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Gerai Digital, Semua Ada Untuk Kamu!",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .background(Color(0xFF3F51B5), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Best Seller\nGerai",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/* =========================
   PREVIEW
========================= */
@Preview(showBackground = true)
@Composable
fun GeraiDigitalScreenPreview() {
    MaterialTheme {
        GeraiDigitalScreen()
    }
}
