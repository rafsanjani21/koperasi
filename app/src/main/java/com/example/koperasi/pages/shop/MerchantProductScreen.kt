package com.example.koperasi.pages.shop


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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.koperasi.R

data class ProductItem(
    val id: String,
    val name: String,
    val price: String,
    val imageRes: Int
)


data class MerchantData(
    val title: String,
    val bannerRes: Int,
    val productsCoffee: List<ProductItem>,
    val productsFood: List<ProductItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantProductScreen(
    merchantId: String,
    onBackClick: () -> Unit,
    onAddToCart: (ProductItem) -> Unit,
    onOpenShoppingList: () -> Unit,
    navController: NavController
) {
    val blue = Color(0xFF4461AD)

    val merchantData = when (merchantId) {
        "kimo" -> MerchantData(
            title = "Kimo Kafe",
            bannerRes = R.drawable.kimomenu,
            productsCoffee = listOf(
                ProductItem("kimo_kopi", "Kopi Kimo", "Rp. 23.000", R.drawable.kopi),
                ProductItem("kimo_latte", "Latte Kimo", "Rp. 25.000", R.drawable.kopi),
                ProductItem("kimo_latte", "Latte Kimo", "Rp. 25.000", R.drawable.kopi),
                ProductItem("kimo_latte", "Latte Kimo", "Rp. 25.000", R.drawable.kopi),
                ProductItem("kimo_latte", "Latte Kimo", "Rp. 25.000", R.drawable.kopi),
                ProductItem("kimo_latte", "Latte Kimo", "Rp. 25.000", R.drawable.kopi),
                ProductItem("kimo_latte", "Latte Kimo", "Rp. 25.000", R.drawable.kopi),
                ProductItem("kimo_latte", "Latte Kimo", "Rp. 25.000", R.drawable.kopi),
                ProductItem("kimo_americano", "Americano", "Rp. 20.000", R.drawable.kopi),
            ),
            productsFood = listOf(
                ProductItem("Snack Kimo","Snack Kimo", "Rp. 15.000", R.drawable.kopi),
                ProductItem("Roti Bakar","Roti Bakar", "Rp. 18.000", R.drawable.kopi),
            )
        )

        "bachra" -> MerchantData(
            title = "Bachra Farm",
            bannerRes = R.drawable.kimomenu,
            productsCoffee = listOf(
                ProductItem("Susu Kambing","Susu Kambing", "Rp. 30.000", R.drawable.kopi),
                ProductItem("Yogurt","Yogurt", "Rp. 22.000", R.drawable.kopi),
            ),
            productsFood = emptyList()
        )

        "burindo" -> MerchantData(
            title = "Burindo",
            bannerRes = R.drawable.kimomenu,
            productsCoffee = listOf(
                ProductItem("Mie Burindo","Mie Burindo", "Rp. 12.000", R.drawable.kopi),
                ProductItem("Bakso","Bakso","Rp. 15.000", R.drawable.kopi),
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

    val hasFood = merchantData.productsFood.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = merchantData.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = blue,
                    titleContentColor = Color.White,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigate("geraimart") },
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onBackClick,
                        enabled = true
                    ) {

                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
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
            var selectedTab by remember { mutableStateOf(0) }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollConnection),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {

                if (hasFood) {
                    item {
                        Column {
                            TabRow(
                                selectedTabIndex = selectedTab,  // ✅ GANTI DARI pagerState
                                containerColor = Color.White,
                                indicator = { tabPositions ->
                                    TabRowDefaults.Indicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                        color = Color(0xFF4461AD)
                                    )
                                }
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },  // ✅ LANGSUNG UPDATE STATE
                                    text = { Text("Drinks") }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },  // ✅ LANGSUNG UPDATE STATE
                                    text = { Text("Food") }
                                )
                            }

                            when (selectedTab) {
                                0 -> ProductSection(
                                    merchantData.productsCoffee,
                                    onAddToCart,
                                    isHorizontal = false
                                )
                                1 -> ProductSection(
                                    merchantData.productsFood,
                                    onAddToCart,
                                    isHorizontal = false
                                )
                            }
                        }
                    }
                } else {
                    item {
                        ProductSection(merchantData.productsCoffee, onAddToCart, isHorizontal = false)
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
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
}

@Composable
fun ProductSection(
    products: List<ProductItem>,
    onAddToCart: (ProductItem) -> Unit,
    isHorizontal: Boolean = false
) {
    if (isHorizontal) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    item = product,
                    onAddToCart = { onAddToCart(product) },
                    isHorizontal = true
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            products.forEach { product ->
                ProductCard(
                    item = product,
                    onAddToCart = { onAddToCart(product) },
                    isHorizontal = false
                )
            }
        }
    }
}


@Composable
fun ProductCard(
    item: ProductItem,
    onAddToCart: () -> Unit,
    isHorizontal: Boolean = false
) {
    if (isHorizontal) {
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
                    contentScale = ContentScale.Crop
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
                        onClick = onAddToCart,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Add", fontSize = 11.sp)
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAddToCart,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Add", fontSize = 11.sp)
                    }
                    Icon(
                        painter = painterResource(R.drawable.heart),
                        contentDescription = "Favorite",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
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

//@Preview(showBackground = true)
//@Composable
//private fun ProductCardPreview() {
//    ProductCard(
//        item = ProductItem(
//            id = "kimo",
//            name = "Chocolate Cake",
//            price = "Rp 25.000",
//            imageRes = R.drawable.kopi // ganti sesuai drawable kamu
//        ),
//        onAddToCart = {}
//    )
//}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MerchantProductPreview() {
    MerchantProductScreen(
        merchantId = "kimo",
        onBackClick = {},
        onAddToCart = { /* no-op */ },
        onOpenShoppingList = { /* no-op */ },
        navController = rememberNavController()
    )
}

