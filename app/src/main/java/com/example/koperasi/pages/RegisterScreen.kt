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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    // State untuk menampilkan dialog Terms & Privacy
    var showTermsDialog by remember { mutableStateOf(false) }

    // State untuk menampilkan error (jika ada)
    var error by remember { mutableStateOf("") }


    Box(modifier = Modifier.fillMaxSize()) {

        // ================= BACKGROUND =================
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

            // ================= HEADER =================
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
                // Tombol kembali ke halaman Login
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
                    color = Color(0xFF4461AD),
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // ================= INFO MESSAGE =================
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

            // ================= ERROR MESSAGE =================
            if (error.isNotEmpty()) {
                Text(error, color = Color.Red)
                Spacer(Modifier.height(8.dp))
            }


            Spacer(Modifier.height(12.dp))

            // ================= CONTENT =================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo aplikasi
                Image(
                    painter = painterResource(id = R.drawable.splash),
                    contentDescription = "Logo",
                    modifier = Modifier.size(215.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Tombol Register dengan Google
                Button(
                    onClick = { showTermsDialog = true },
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

                // Navigasi ke halaman Login
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
                        color = Color(0xFF4461AD),
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                }

            }
            Spacer(modifier = Modifier.weight(1f))

            // ================= TERMS DIALOG =================
            if (showTermsDialog) {
                TermsAndPrivacyDialog(
                    onDismiss = { showTermsDialog = false },
                    onAgree = {
                        showTermsDialog = false
                        onGoogleRegister()
                    }
                )
            }

        }

    }
}

/**
 * TermsAndPrivacyDialog
 *
 * Dialog yang menampilkan:
 * - Terms of Service
 * - Privacy Policy
 * - Checkbox persetujuan pengguna
 *
 * Tombol "Lanjut" hanya aktif jika checkbox disetujui.
 *
 * @param onDismiss Callback saat dialog ditutup
 * @param onAgree Callback saat user menyetujui dan melanjutkan
 */
@Composable
fun TermsAndPrivacyDialog(
    onDismiss: () -> Unit,
    onAgree: () -> Unit
) {
    // State checkbox persetujuan
    var checked by remember { mutableStateOf(false) }
    // State scroll untuk konten panjang
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Terms of Service & Privacy Policy",
                fontWeight = FontWeight.Bold
            )
        },
        // ================= CONTENT =================
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp) // ⬅️ BATAS TINGGI
                    .verticalScroll(scrollState)
            ) {

                /* ========== TERMS OF SERVICE ========== */

                Text(
                    "Term Of Service",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "I. Komitmen & Akad Anggota",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text("• Kesediaan Iuran: Saya setuju membayar iuran sebesar Rp 30.000 per bulan.")
                Spacer(Modifier.height(4.dp))
                Text("• Pernyataan Akad: Dana Rp 30.000 adalah Hibah/Sedekah/Donasi dan tidak dapat ditarik kembali.")
                Spacer(Modifier.height(4.dp))
                Text("• Pemanfaatan Dana: 50% untuk Pool Reward (Hadiah) dan 50% untuk dana produktif (Pinjaman UMKM).")
                Spacer(Modifier.height(4.dp))
                Text("• Persetujuan Syarat & Ketentuan: Saya bersedia mematuhi prinsip gotong royong dan transparansi Koperasi Gerai.")

                Spacer(Modifier.height(12.dp))

                Text(
                    "II. Instruksi Alur Pendaftaran",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text("1. Calon anggota mengisi biodata dan mengunggah dokumen melalui aplikasi.")
                Text("2. Sistem melakukan verifikasi data untuk memastikan keaslian NIK.")
                Text("3. Setelah terverifikasi, anggota melakukan pembayaran donasi pertama.")
                Text("4. Anggota resmi masuk ke ekosistem Closed-Loop dan berhak atas hadiah serta akses pinjaman UMKM.")

                Spacer(Modifier.height(16.dp))

                /* ========== PRIVACY POLICY ========== */
                Text(
                    "Provacy Notice",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "1. Komitmen Perlindungan Data",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "Koperasi berkomitmen untuk melindungi privasi dan data pribadi Pengguna Aplikasi Koperasi [Nama Aplikasi]. Kebijakan Privasi ini menjelaskan bagaimana data pribadi dikumpulkan, digunakan, disimpan, dan dilindungi sesuai dengan peraturan perundang-undangan yang berlaku, termasuk Undang-Undang Perlindungan Data Pribadi."
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "2. Jenis Data yang Dikumpulkan",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "Dalam penyelenggaraan layanan, Koperasi dapat mengumpulkan data identitas Pengguna, data keanggotaan koperasi, data transaksi simpanan dan pinjaman, serta data teknis yang dihasilkan dari penggunaan Aplikasi. Data dikumpulkan secara sah dan relevan dengan tujuan penggunaan Aplikasi."
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "3. Tujuan Penggunaan Data",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "Data pribadi Pengguna digunakan untuk keperluan administrasi keanggotaan, pengelolaan transaksi koperasi, penyediaan layanan dan dukungan kepada Pengguna, penyampaian informasi koperasi, serta pemenuhan kewajiban hukum yang berlaku."
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "4. Keamanan dan Penyimpanan Data",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "Koperasi menerapkan langkah-langkah keamanan teknis dan organisasi yang wajar untuk melindungi data pribadi dari akses tidak sah, kehilangan, penyalahgunaan, atau kebocoran. Data pribadi disimpan selama Pengguna masih terdaftar sebagai anggota koperasi atau sesuai dengan ketentuan hukum."
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "5. Pembagian Data",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "Koperasi tidak akan membagikan data pribadi Pengguna kepada pihak ketiga tanpa persetujuan Pengguna, kecuali jika diwajibkan oleh peraturan perundang-undangan atau diperlukan untuk kepentingan operasional koperasi yang sah."
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "6. Hak Pengguna atas Data Pribadi",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "Pengguna memiliki hak untuk mengakses, memperbarui, menarik persetujuan, serta mengajukan permintaan penghapusan data pribadi sesuai dengan ketentuan hukum yang berlaku. Permintaan terkait hak data pribadi dapat diajukan melalui kontak resmi Koperasi."
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "7. Perubahan Kebijakan dan Kontak",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                Text(
                    "Kebijakan Privasi ini dapat diperbarui sewaktu-waktu dan akan diinformasikan melalui Aplikasi. Untuk pertanyaan atau permintaan terkait Kebijakan Privasi ini, Pengguna dapat menghubungi Koperasi melalui [email@koperasi.co.id] atau [nomor kontak koperasi]."
                )
                Spacer(Modifier.height(6.dp))

                Spacer(Modifier.height(16.dp))

                /* ========== CHECKBOX ========== */
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Saya telah membaca dan menyetujui Terms of Service dan Privacy Policy",
                        fontSize = 13.sp
                    )
                }
            }
        },

        // ================= ACTION BUTTON =================
        confirmButton = {
            TextButton(
                enabled = checked,
                onClick = onAgree
            ) {
                Text(
                    "Lanjut",
                    fontWeight = FontWeight.Bold,
                    color = if (checked) Color(0xFF4461AD) else Color.Gray
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    KoperasiTheme {
        RegisterScreen(onNavigateLogin = {}, onGoogleRegister = {})
    }
}



