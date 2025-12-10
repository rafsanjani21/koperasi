package com.example.koperasi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R
import com.example.koperasi.ui.theme.KoperasiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileDataScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxWidth(),
        topBar = {
            Box(
                modifier = modifier.padding(6.dp)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Data User",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF68E1E)
                        )
                    },
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    modifier = modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                        .clickable(onClick = { /* Handle back action */ })
                )
            }
        },
    ) { paddingValues ->
        Surface(
            modifier = modifier.padding(paddingValues)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)
            ) {
                UserProfilePrivateInfo(modifier = modifier)
                UserProfileAddressInfo(modifier = modifier)
                UserProfileKTP(modifier = modifier)
                Button(
                    modifier = modifier.width(260.dp).height(IntrinsicSize.Max),
                    onClick = {/**/},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF0000)
                    )
                ) {
                    Text(
                        text = "Log Out",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfilePrivateInfo(
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        Text(
            text = "Informasi Pribadi",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF171717)
        )
        HorizontalDivider(modifier = modifier.padding(vertical = 6.dp), color = Color(0xFFD6D6D6))
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    text = "Jenis Kelamin",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4D4D4D)
                )
                Text(
                    text = "Laki-laki",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF171717)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    text = "NIK",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4D4D4D)
                )
                Text(
                    text = "3326160608070197",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF171717)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    text = "Nomor Telepon",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4D4D4D)
                )
                Text(
                    text = "+62 812 4567 890",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF171717)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    text = "Tanggal Lahir",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4D4D4D)
                )
                Text(
                    text = "30 Februari 1990",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF171717)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    text = "Pekerjaan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4D4D4D)
                )
                Text(
                    text = "Peternak Lele",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF171717)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    text = "Pendidikan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4D4D4D)
                )
                Text(
                    text = "S1 - Ilmu Komputer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF171717)
                )
            }
        }
    }
}

@Composable
fun UserProfileAddressInfo(
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        Text(
            text = "Alamat",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF171717)
        )
        HorizontalDivider(modifier = modifier.padding(vertical = 6.dp), color = Color(0xFFD6D6D6))
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Gang Kana Nomor 123 RT 01 RW 02",
                fontSize = 14.sp,
                color = Color(0xFF4D4D4D)
            )
            Text(
                text = "Rawamangun, KOTA JAKARTA TIMUR",
                fontSize = 14.sp,
                color = Color(0xFF4D4D4D)
            )
        }
    }   
}

@Composable
fun UserProfileKTP(
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        Text(
            text = "KTP",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF171717),
            modifier = modifier.align(Alignment.Start)
        )
        HorizontalDivider(modifier = modifier.padding(vertical = 6.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ktp_placeholder),
                contentDescription = "KTP",
                modifier = modifier.width(IntrinsicSize.Max)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun UserProfileDataScreenPreview() {
    KoperasiTheme {
        UserProfileDataScreen()
    }
}