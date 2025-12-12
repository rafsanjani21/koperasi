package com.example.koperasi.pages

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R

data class ProductItem(
    val name: String,
    val price: String,
    val imageRes: Int
)

private data class MerchantData(
    val title: String,
    val bannerRes: Int,
    val productsCoffee: List<ProductItem>,
    val productsFood: List<ProductItem>
)

@Composable
fun MerchantProductScreen(
    merchantId: String,
    onBackClick: () -> Unit
) {
    val orange = Color(0xFFF68E1E)

    val merchantData = when (merchantId) {
        "kimo" -> MerchantData(
            title = "Kimo Kafe",
            bannerRes = R.drawable.kimomenu,
            productsCoffee = listOf(
                ProductItem("Kopi Kimo", "Rp. 23.000", R.drawable.kopi),
                ProductItem("Latte Kimo", "Rp. 25.000", R.drawable.kopi),
                ProductItem("Americano", "Rp. 20.000", R.drawable.kopi),
            ),
            productsFood = listOf(
                ProductItem("Snack Kimo", "Rp. 15.000", R.drawable.kopi),
                ProductItem("Roti Bakar", "Rp. 18.000", R.drawable.kopi),
            )
        )

        "bachra" -> MerchantData(
            title = "Bachra Farm",
            bannerRes = R.drawable.kimomenu,
            productsCoffee = listOf(
                ProductItem("Susu Kambing", "Rp. 30.000", R.drawable.kopi),
                ProductItem("Yogurt", "Rp. 22.000", R.drawable.kopi),
            ),
            productsFood = emptyList()
        )

        "burindo" -> MerchantData(
            title = "Burindo",
            bannerRes = R.drawable.kimomenu,
            productsCoffee = listOf(
                ProductItem("Mie Burindo", "Rp. 12.000", R.drawable.kopi),
                ProductItem("Bakso", "Rp. 15.000", R.drawable.kopi),
            ),
            productsFood = emptyList()
        )

        else -> MerchantData(
            title = "Toko",
            bannerRes = R.drawable.splash,
            productsCoffee = emptyList(),
            productsFood = emptyList()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
        val listState = rememberLazyListState()

        var isScrollingUp by remember { mutableStateOf(true) }
        var prevIndex by remember { mutableStateOf(0) }
        var prevOffset by remember { mutableStateOf(0) }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    isScrollingUp = if (index != prevIndex) {
                        index < prevIndex
                    } else {
                        offset < prevOffset
                    }
                    prevIndex = index
                    prevOffset = offset
                }
        }

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
                MerchantHeader(
                    title = merchantData.title,
                    bannerRes = merchantData.bannerRes,
                    onBackClick = onBackClick
                )
            }

            item { Spacer(Modifier.height(80.dp)) }

            item {
                Text(
                    text = merchantData.title,
                    color = orange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            if (merchantData.productsCoffee.isNotEmpty()) {
                item { ProductSection("Coffee", merchantData.productsCoffee) }
                item { Spacer(Modifier.height(16.dp)) }
            }

            if (merchantData.productsFood.isNotEmpty()) {
                item { ProductSection("Food", merchantData.productsFood) }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        val showTopHeader = isScrollingUp && (
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
                )

        AnimatedVisibility(
            visible = showTopHeader,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Surface(
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = merchantData.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = orange
                    )
                }
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
            ShoppingListBar(orange = orange)
        }
    }
}

// ==== komponen bantu ====

@Composable
private fun MerchantHeader(
    title: String,
    bannerRes: Int,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Image(
            painter = painterResource(bannerRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = 8.dp, top = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .offset(y = 60.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE0E0E0),
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProductSection(
    title: String,
    products: List<ProductItem>
) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            ProductCard(product)
        }
    }
}

@Composable
private fun ProductCard(item: ProductItem) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.price,
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.heart),
                    contentDescription = "Favorite",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )

                OutlinedButton(
                    onClick = { /* TODO tambahkan ke cart */ },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Add", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ShoppingListBar(
    modifier: Modifier = Modifier,
    orange: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = orange,
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
fun MerchantProductPreview() {
    MerchantProductScreen(
        merchantId = "kimo",
        onBackClick = {}
    )
}
