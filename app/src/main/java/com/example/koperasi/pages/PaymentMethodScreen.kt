package com.example.koperasi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R
import com.example.koperasi.ui.theme.KoperasiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    modifier: Modifier = Modifier
) {
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
            modifier = modifier.padding(paddingValues).fillMaxSize()
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
                    PaymentButton(
                        buttonName = "BCA",
                        onClick = {/**/}
                    )
                    PaymentButton(
                        buttonName = "Mandiri",
                        onClick = {/**/}
                    )
                    PaymentButton(
                        buttonName = "GoPay",
                        onClick = {/**/}
                    )
                    PaymentButton(
                        buttonName = "Koperasi Gerai Pay",
                        onClick = {/**/}
                    )
                }
                Spacer(modifier = modifier.height(96.dp))
                Button(
                    modifier = modifier.width(260.dp).height(IntrinsicSize.Max),
                    onClick = {/**/},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF68E1E)
                    )
                ) {
                    Text(
                        text = "Pay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentButton(
    buttonName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE2E2E2),
            contentColor = Color(0XFF171717)
        ),
        modifier = modifier
            .height(52.dp)
            .width(280.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = buttonName,
                color = Color(0xFF171717),
                fontSize = 15.sp,
                modifier = modifier.align(Alignment.CenterStart)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentMethodScreenPreview() {
    KoperasiTheme {
        PaymentMethodScreen()
    }
}