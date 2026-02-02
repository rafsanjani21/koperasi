package com.example.koperasi.pages

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateRegister: () -> Unit,
    onGoogleLogin: suspend () -> Unit
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var shouldLoginGoogle by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // trigger login
            shouldLoginGoogle = true
        } else {
            errorMessage = "Permission denied"
            showErrorDialog = true
        }
    }

    // Trigger Google login in composable context
    if (shouldLoginGoogle) {
        LaunchedEffect(Unit) {
            try {
                onGoogleLogin()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Something went wrong"
                showErrorDialog = true
            } finally {
                shouldLoginGoogle = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background blur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x0FF5F5F5))
                .blur(30.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF4461AD),
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // KONTEN TENGAH
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
                    onClick = {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(50.dp)),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.google),
                        tint = Color.Unspecified,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp)
                    )
                    Text("Continue With Google", color = Color(0xFF8C8C8C))
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.clickable { onNavigateRegister() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don’t have an account?",
                        color = Color.Black
                    )
                    Text(
                        text = "Sign Up",
                        color = Color(0xFF4461AD),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // ALERT DIALOG ERROR
        if (showErrorDialog && errorMessage != null) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text(text = "Login Error") },
                text = { Text(text = errorMessage ?: "Unknown error") }
            )
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
