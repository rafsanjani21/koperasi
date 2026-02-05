package com.example.koperasi.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Membuat URI sementara untuk file gambar menggunakan FileProvider.
 *
 * Biasanya digunakan untuk:
 * - Kamera (ACTION_IMAGE_CAPTURE)
 * - Upload foto (KTP, foto profil, dll)
 *
 * Mekanisme:
 * - File dibuat di folder cache internal aplikasi (`cacheDir/images`)
 * - Nama file bersifat unik (prefix + timestamp)
 * - URI dihasilkan melalui FileProvider agar aman (tidak expose file://)
 *
 * ⚠️ Catatan:
 * - File ini bersifat sementara (cache), bisa dihapus sistem kapan saja
 * - Pastikan FileProvider sudah didefinisikan di AndroidManifest.xml
 *
 * @param context Context aplikasi
 * @param prefix prefix nama file (contoh: "ktp", "profile")
 * @return Uri aman yang bisa digunakan untuk intent kamera / upload
 */
fun createImageUri(context: Context, prefix: String): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}