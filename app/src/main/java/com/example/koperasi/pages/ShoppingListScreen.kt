package com.example.koperasi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.navigation.CartItem
import com.example.koperasi.navigation.CartState


@Composable
fun ShoppingListScreen(
    cartState: CartState,
    onBackClick: () -> Unit,
    onPayClick: () -> Unit
) {
    val orange = Color(0xFFF68E1E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Shopping List",
                    color = orange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(cartState.items, key = { it.id }) { item ->
                    ShoppingListItemCard(
                        item = item,
                        onMinus = { cartState.decrement(item.id) },
                        onPlus = { cartState.increment(item.id) },
                        onNotesChange = { cartState.updateNotes(item.id, it) }
                    )
                }
            }
        }

        // Pay button fixed bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            shape = RoundedCornerShape(50),
            color = orange,
            shadowElevation = 8.dp,
            onClick = onPayClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pay >>>",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ShoppingListItemCard(
    item: CartItem,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onNotesChange: (String) -> Unit
) {
    val orange = Color(0xFFF68E1E)

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFD9D9D9)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .heightIn(min = 110.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .size(177.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier
                .weight(1f)
                .align(Alignment.Bottom)) {
                // qty row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SmallSquareButton(text = "−", onClick = onMinus)
                    Spacer(Modifier.width(10.dp))
                    Text(text = item.qty.toString(), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    SmallSquareButton(text = "+", onClick = onPlus)
                }

                // Added button look
                Surface(
                    shape = RoundedCornerShape(50),
                    color = orange
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Added", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = item.notes,
                    onValueChange = onNotesChange,
                    placeholder = { Text("Notes...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 38.dp),
                    shape = RoundedCornerShape(50),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun SmallSquareButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShoppingListScreenPreview() {
    val cartState = CartState().apply {
        addOrIncrement(
            id = "kimo_kopi",
            name = "Kopi Kimo",
            price = "Rp. 23.000",
            imageRes = com.example.koperasi.R.drawable.kopi
        )
        addOrIncrement(
            id = "kimo_snack",
            name = "Snack Kimo",
            price = "Rp. 15.000",
            imageRes = com.example.koperasi.R.drawable.kopi
        )
        addOrIncrement(
            id = "kimo_kopi",
            name = "Kopi Kimo",
            price = "Rp. 23.000",
            imageRes = com.example.koperasi.R.drawable.kopi
        )
        updateNotes("kimo_kopi", "Less sugar")
    }

    ShoppingListScreen(
        cartState = cartState,
        onBackClick = {},
        onPayClick = {}
    )
}

