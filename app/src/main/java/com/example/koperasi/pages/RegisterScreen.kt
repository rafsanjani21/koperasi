package com.example.koperasi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R
import com.example.koperasi.ui.theme.KoperasiTheme



@Composable
fun RegisterScreen(
    infoMessage: String? = null,
    onNavigateLogin: () -> Unit,
    onGoogleRegister: () -> Unit
) {

    var error by remember { mutableStateOf("") }


    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0x0FF5F5F5))
                .blur(30.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {

            //HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 24.dp,
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Tombol back
                IconButton(
                    onClick = { onNavigateLogin() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(60.dp)
                    )
                }

                // Title di tengah
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFF68E1E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            val showInfo = remember(infoMessage) { infoMessage ?: "" }

            if (showInfo.isNotEmpty()) {
                Text(
                    text = showInfo,
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))
            }


            if (error.isNotEmpty()) {
                Text(error, color = Color.Red)
                Spacer(Modifier.height(8.dp))
            }


            Spacer(Modifier.height(12.dp))

            //KONTEN TENGAH
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash),
                    contentDescription = "Logo",
                    modifier = Modifier.size(215.dp)
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { onGoogleRegister() },
                    modifier = Modifier
                        .width(280.dp)
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(0.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.google),
                        tint = Color.Unspecified,
                        contentDescription = null,
                        modifier = Modifier
                            .size(34.dp)
                    )
                    Spacer(Modifier.width(0.dp))
                    Text("Register With Google", color = Color(0xFF8C8C8C), fontSize = 15.sp)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.clickable { onNavigateLogin() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        color = Color.Black
                    )
                    Text(
                        text = "Log In",
                        color = Color(0xFFF68E1E),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }

            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    KoperasiTheme {
        RegisterScreen(onNavigateLogin = {}, onGoogleRegister = {})
    }
}



