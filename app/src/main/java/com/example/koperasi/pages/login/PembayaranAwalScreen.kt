package com.example.koperasi.pages.login

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koperasi.R
import java.io.OutputStream

@Composable
fun PaymentScreen(
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var uploadSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 📁 Launcher pilih gambar
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                uploadSuccess = true
            }
        }

    val primaryBlue = Color(0xFF5068A9)
    val silverColor = Color(0xFFC0C0C0)
    val white = Color(0xFFFFFFFF)


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = primaryBlue
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pembayaran",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Scan untuk melakukan pembayaran!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // QR IMAGE
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {

                if (isPreview) {
                    // Dummy box saat preview supaya tidak crash
                    Text(
                        text = "QR Preview",
                        color = Color.Gray
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.qr),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(180.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DOWNLOAD QR
            Button(
                onClick = {
                    if (!isPreview) {
                        downloadQr(context)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Download, null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Unduh QR", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Unggah bukti pembayaran:",
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                Icon(Icons.Default.Folder, null, tint = primaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unggah Berkas", color = primaryBlue)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preview foto yang dipilih
            selectedImageUri?.let {
                val bitmap = BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(it)
                )

                bitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { },
                enabled = uploadSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uploadSuccess) white else silverColor,
                    disabledContainerColor = silverColor
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Selesai")
                }
            }
        }
    }
}

/**
 * Function Download QR ke Gallery
 */
fun downloadQr(context: Context) {

    val bitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.qr
    )

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "QR_Koperasi.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES
        )
    }

    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )

    uri?.let {
        val outputStream: OutputStream? =
            context.contentResolver.openOutputStream(it)

        outputStream?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PaymentScreenPreview() {
    PaymentScreen(
        onBackClick = {}
    )
}