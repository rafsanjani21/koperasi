package com.example.koperasi.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.koperasi.R
import com.example.koperasi.ui.theme.KoperasiTheme


enum class PaymentMethod {
    BCA,
    Mandiri,
    Gopay,
    KoperasiGeraiPay
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    onPay: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    val listPaymentMethod = listOf(
        PaymentMethod.BCA,
        PaymentMethod.Mandiri,
        PaymentMethod.Gopay,
        PaymentMethod.KoperasiGeraiPay
    )

    Scaffold (
        modifier = modifier.fillMaxWidth(),
        topBar = {
            Box(
                modifier = modifier.padding(6.dp)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Payment Method",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF68E1E)
                        )
                    },
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    modifier = modifier
                        .size(32.dp)
                        .align(Alignment.CenterStart)
                        .clickable(onClick = { /* Handle back action */ })
                )
            }
        },
    ) { paddingValues ->
        Surface(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier.padding(bottom = 12.dp)
            ) {
                Image(
                    painterResource(id = R.drawable.gerai),
                    contentDescription = "Logo Gerai",
                    modifier = modifier.size(180.dp)
                )
                Spacer(modifier = modifier.height(24.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    listPaymentMethod.forEach { paymentMethod ->
                        PaymentOptionItem(
                            method = paymentMethod,
                            isSelected = selectedPaymentMethod == paymentMethod,
                            onSelect = { selectedPaymentMethod = paymentMethod },
                            modifier = modifier
                        )
                    }
                }
                Spacer(modifier = modifier.height(96.dp))
                Button(
                    onClick = { selectedPaymentMethod?.let { onPay(it) } },
                    enabled = selectedPaymentMethod != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF68E1E),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = modifier.width(320.dp).height(52.dp)
                ) {
                    Text("Pay")
                }
            }
        }
    }
}

@Composable
fun PaymentOptionItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier
) {
    val borderColor = if (isSelected) Color(0xFFDE8321)
    else Color.Gray.copy(alpha = 0.2f)

    val backgroundButtonColor = if (isSelected) Color(0xFFF29029)
    else Color.Gray.copy(alpha = 0.1f)

    val textWeight = if (isSelected) FontWeight.Medium
    else FontWeight.Normal

    Card(
        modifier = modifier
            .width(320.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundButtonColor)
    ) {
        Row (
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = method.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = textWeight
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPaymentScreen() {
    MaterialTheme {
        PaymentMethodScreen(
            onPay = {}
        )
    }
}

