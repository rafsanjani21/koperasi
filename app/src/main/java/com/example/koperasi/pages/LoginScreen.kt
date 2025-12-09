package com.example.koperasi.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R

@Composable
fun LoginScreen(
    onNavigateRegister: () -> Unit,
    onGoogleLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                .padding(24.dp)
        ) {
            //HEADER
            Text(
                "Log In",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF68E1E),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.weight(1f))

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
                    onClick = { onGoogleLogin() },
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
                    Text("Continue With Google", color = Color(0xFF8C8C8C), fontSize = 15.sp)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.clickable { onNavigateRegister() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don’t have an account? ",
                        color = Color.Black
                    )
                    Text(
                        text = "Sign Up",
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
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateRegister = {},
        onGoogleLogin = {}
    )
}
