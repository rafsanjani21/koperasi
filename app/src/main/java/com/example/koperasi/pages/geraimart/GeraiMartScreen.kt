package com.example.koperasi.pages.geraimart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.koperasi.R
import com.example.koperasi.pages.main.AppShortcutItem
import com.example.koperasi.pages.shop.MerchantData
import com.example.koperasi.pages.shop.ProductItem
import com.example.koperasi.pages.shop.ProductSection

@Composable
fun GeraiMartScreen(
    onBackClick: () -> Unit,
    onAddToCart: (ProductItem) -> Unit,
    onOpenShoppingList: () -> Unit,
    navController: NavController
) {
    val blue = Color(0xFF4461AD)

    val merchantData = MerchantData(
        title = "Gerai Mart",
        bannerRes = R.drawable.gerai_mart_banner,
        productsCoffee = listOf(
            ProductItem("gm_beras", "Beras Premium", "Rp. 50.000", R.drawable.kopi),
            ProductItem("gm_minyak", "Minyak Goreng", "Rp. 35.000", R.drawable.kopi),
            ProductItem("gm_gula", "Gula Pasir", "Rp. 15.000", R.drawable.kopi),
        ),
        productsFood = listOf(
            ProductItem("gm_telur", "Telur Ayam", "Rp. 25.000", R.drawable.kopi),
            ProductItem("gm_susu", "Susu Cair", "Rp. 20.000", R.drawable.kopi),
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
        val listState = rememberLazyListState()

        // ===== threshold hide/show =====
        val density = LocalDensity.current
        val thresholdPx = with(density) { 20.dp.toPx() }

        var showShoppingBar by remember { mutableStateOf(true) }
        var accumulatedDy by remember { mutableStateOf(0f) }

        val scrollConnection = remember(thresholdPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val dy = available.y

                    // dy negatif = user scroll ke bawah (konten naik)
                    // dy positif = user scroll ke atas (konten turun)
                    accumulatedDy += dy

                    if (accumulatedDy <= -thresholdPx) {
                        showShoppingBar = false
                        accumulatedDy = 0f
                    } else if (accumulatedDy >= thresholdPx) {
                        showShoppingBar = true
                        accumulatedDy = 0f
                    }

                    return Offset.Zero
                }
            }
        }

        // ===== CONTENT SCROLL (LAZYCOLUMN) =====
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollConnection),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            item {
                GeraiMartHeader(
                    navController = navController,
                    onBackClick = onBackClick)
            }

            item { Spacer(Modifier.height(90.dp)) }

            item{
                Text(
                    text = "Gerai Mart",
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(start = 45.dp, bottom = 12.dp),
                    color = Color(0xFF4461AD)
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Belanja Lebih Mudah di Gerai Mart!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        text = "Sekali mampir, semua belanja beres!",
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            if (merchantData.productsCoffee.isNotEmpty()) {
                item { ProductSection(merchantData.productsCoffee, onAddToCart, isHorizontal = true) }
                item { Spacer(Modifier.height(16.dp)) }
            }

            if (merchantData.productsFood.isNotEmpty()) {
                item { ProductSection( merchantData.productsFood, onAddToCart, isHorizontal = true) }
            }

        }

        // ===== FIXED SHOPPING LIST (anim) =====
        AnimatedVisibility(
            visible = showShoppingBar,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 50.dp)
        ) {
            ShoppingListBar(
                blue = blue,
                onClick = onOpenShoppingList
            )
        }
    }
}

// ==== komponen bantu ====

@Composable
private fun GeraiMartHeader(
    navController: NavController,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.gerai_mart_banner),
            contentDescription = "geraimartbanner",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Gerai Mart",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = onBackClick,
                enabled = true
            ) {

            }
        }

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
                    .height(150.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.width(204.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppShortcutItem("Kimo Cafe", R.drawable.kimo_logo) {
                        navController.navigate("merchant/kimo")
                    }

                    AppShortcutItem("Bachra Farm", R.drawable.bachrafarm_logo) {
                        navController.navigate("merchant/bachra")
                    }

                    AppShortcutItem("Burindo", R.drawable.burindo_logo) {
                        navController.navigate("merchant/burindo")
                    }
                }
            }
        }
    }
}


@Composable
private fun ShoppingListBar(
    modifier: Modifier = Modifier,
    blue: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = blue,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .width(221.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shopping List",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Icon(
                painterResource(R.drawable.cart),
                contentDescription = "Cart",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GeraiMartPreview() {
    GeraiMartScreen(
        onBackClick = {},
        onAddToCart = { /* no-op */ },
        onOpenShoppingList = { /* no-op */ },
        navController = rememberNavController()
    )
}

