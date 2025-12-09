package com.example.koperasi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R
import com.example.koperasi.ui.theme.KoperasiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Box(
                modifier = modifier.padding(6.dp)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "User",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF68E1E)
                        )
                    },
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    modifier = modifier.size(40.dp).align(Alignment.CenterStart).clickable(onClick = { /* Handle back action */ })
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Surface(
            modifier = modifier.padding(innerPadding)
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = modifier.fillMaxSize().padding(vertical = 12.dp, horizontal = 48.dp)
            ){
                Image(
                    painter = painterResource(id = R.drawable.placeholder_profile),
                    contentDescription = "Profile",
                    modifier = modifier.size(140.dp)
                )
                Text(
                    text = "John Doe",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171717)
//                  color = Color(0xFFF68E1E)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(Color(0xFFE2E2E2)),
                        modifier = modifier.fillMaxWidth().height(52.dp),
                        onClick = {/**/}
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Edit Profile",
                                color = Color(0XFF171717),
                                fontSize = 15.sp,
                                modifier = modifier.align(Alignment.CenterStart)
                            )
                        }
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(Color(0xFFE2E2E2)),
                        modifier = modifier.fillMaxWidth().height(52.dp),
                        onClick = {/**/}
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "My Activity",
                                color = Color(0XFF171717),
                                fontSize = 15.sp,
                                modifier = modifier.align(Alignment.CenterStart)
                            )
                        }
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(Color(0xFFE2E2E2)),
                        modifier = modifier.fillMaxWidth().height(52.dp),
                        onClick = {/**/}
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Promos",
                                color = Color(0XFF171717),
                                fontSize = 15.sp,
                                modifier = modifier.align(Alignment.CenterStart)
                            )
                        }
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(Color(0xFFE2E2E2)),
                        modifier = modifier.fillMaxWidth().height(52.dp),
                        onClick = {/**/}
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Change Language",
                                color = Color(0XFF171717),
                                fontSize = 15.sp,
                                modifier = modifier.align(Alignment.CenterStart)
                            )
                        }
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(Color(0xFFE2E2E2)),
                        modifier = modifier.fillMaxWidth().height(52.dp),
                        onClick = {/**/}
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Privacy Policy",
                                color = Color(0XFF171717),
                                fontSize = 15.sp,
                                modifier = modifier.align(Alignment.CenterStart)
                            )
                        }
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(Color(0xFFE2E2E2)),
                        modifier = modifier.fillMaxWidth().height(52.dp),
                        onClick = {/**/}
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Terms of Service",
                                color = Color(0XFF171717),
                                fontSize = 15.sp,
                                modifier = modifier.align(Alignment.CenterStart)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileScreenPreview() {
    KoperasiTheme() {
        UserProfileScreen()
    }
}