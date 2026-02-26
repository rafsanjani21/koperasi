package com.example.koperasi.pages.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.example.koperasi.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IuranAwalScreen(
    onBack: () -> Unit,
    onBayar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ===== BACKGROUND BIRU DENGAN CURVE =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    color = Color(0xFF4B67B0),
                    shape = RoundedCornerShape(
                        bottomStart = 50.dp,
                        bottomEnd = 50.dp
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
        ) {

            Spacer(Modifier.height(16.dp))

            // ===== TOP BAR =====
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    "Iuran Awal Anggota",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }


            Spacer(Modifier.height(32.dp))

            // ===== ICON UANG =====
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.uang),
                    contentDescription = "Money",
                    modifier = Modifier.size(100.dp)
                )
            }


            Spacer(Modifier.height(32.dp))

            // ===== CARD DETAIL =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Detail Pembayaran",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    RowItem("Iuran Awal", "Rp 100.000")

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Iuran Bulanan",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(8.dp))

                    SubRowItem("• Iuran Bulanan", "Rp 30.000")
                    SubRowItem("• Donasi", "Rp 30.000")

                    Spacer(Modifier.height(16.dp))

                    Divider()

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Rp 160.000",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ===== BUTTON BAYAR =====
            Button(
                onClick = onBayar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4B67B0)
                )
            ) {
                Text(
                    "Bayar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RowItem(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 13.sp)
        Text(value, fontSize = 13.sp)
    }
}

@Composable
private fun SubRowItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, color = Color.Gray)
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_4
)
@Composable
fun IuranAwalScreenPreview() {
    MaterialTheme {
        IuranAwalScreen(
            onBack = {},
            onBayar = {}
        )
    }
}

