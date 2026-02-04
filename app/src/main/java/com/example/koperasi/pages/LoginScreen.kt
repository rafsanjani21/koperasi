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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R
import com.example.koperasi.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateRegister: () -> Unit,
    onLoginGoogle: () -> Unit
) {

    // 🔥 SINGLE SOURCE OF TRUTH
    val error by viewModel.errorMessage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // 🔥 REAKSI SAAT ERROR BERUBAH
    LaunchedEffect(error) {
        showDialog = error != null
    }

    // 🔥 PERMISSION LAUNCHER
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onLoginGoogle()
        } else {
            viewModel.setError("Izin lokasi diperlukan untuk login")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ================= BACKGROUND =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .blur(30.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ================= TITLE =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log In",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4461AD)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ================= CONTENT =================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.splash),
                    contentDescription = "Logo",
                    modifier = Modifier.size(215.dp)
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        permissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(50.dp)),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.google),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Continue with Google", color = Color(0xFF8C8C8C))
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.clickable { onNavigateRegister() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don’t have an account? ")
                    Text(
                        "Sign Up",
                        color = Color(0xFF4461AD),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // ================= ERROR DIALOG =================
        if (showDialog && error != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    viewModel.clearError()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            viewModel.clearError()
                        }
                    ) {
                        Text("OK")
                    }
                },
                title = { Text("Login Gagal") },
                text = { Text(error!!) }
            )
        }
    }
}
