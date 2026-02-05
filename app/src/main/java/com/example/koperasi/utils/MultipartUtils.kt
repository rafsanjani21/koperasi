package com.example.koperasi.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/* =========================================================
 * TEXT REQUEST BODY
 * ========================================================= */

/**
 * Mengubah String menjadi RequestBody bertipe text/plain.
 *
 * Digunakan untuk:
 * - Field text pada multipart request
 * - Upload form data ke backend (name, nik, alamat, dll)
 *
 * Contoh penggunaan:
 * ```
 * api.registerUser(
 *   name = form.nama.toTextBody()
 * )
 * ```
 *
 * @receiver String teks yang akan dikirim ke backend
 * @return RequestBody bertipe text/plain
 */
fun String.toTextBody(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())

/* =========================================================
 * FILE MULTIPART
 * ========================================================= */

/**
 * Membuat MultipartBody.Part dari URI (biasanya hasil kamera / galeri).
 *
 * Alur kerja:
 * 1. Membuka InputStream dari URI
 * 2. Menyalin isi file ke temporary file di cacheDir
 * 3. Menentukan MIME type dari ContentResolver
 * 4. Membungkus file menjadi MultipartBody.Part
 *
 * Digunakan untuk:
 * - Upload foto KTP
 * - Upload foto profil
 *
 * ⚠️ Catatan:
 * - File disimpan di cache, bersifat sementara
 * - Pastikan permission URI sudah diberikan (FLAG_GRANT_READ_URI_PERMISSION)
 *
 * @param context Context aplikasi
 * @param uri URI file (content://)
 * @param fieldName nama field multipart sesuai backend
 * @return MultipartBody.Part siap dikirim ke API
 */
fun filePart(
    context: Context,
    uri: Uri,
    fieldName: String
): MultipartBody.Part {

    // Ambil input stream dari URI
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: error("Cannot open uri")

    // File sementara di cache
    val tempFile = File(
        context.cacheDir,
        "upload_${System.currentTimeMillis()}.jpg"
    )

    // Salin isi URI ke file lokal
    tempFile.outputStream().use { output ->
        inputStream.use { input ->
            input.copyTo(output)
        }
    }

    // Tentukan MIME type (fallback ke image/jpeg)
    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
    // Bungkus file menjadi RequestBody
    val body = tempFile.asRequestBody(mime.toMediaType())

    // Buat MultipartBody.Part
    return MultipartBody.Part.createFormData(
        fieldName,
        tempFile.name,
        body
    )
}

/* =========================================================
 * URI → TEMP FILE (INTERNAL HELPER)
 * ========================================================= */

/**
 * Mengonversi URI menjadi File sementara di cacheDir.
 *
 * Biasanya digunakan jika API membutuhkan objek File
 * (bukan MultipartBody.Part langsung).
 *
 * @param context Context aplikasi
 * @param uri URI sumber file
 * @return File sementara hasil salinan
 */
private fun uriToTempFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: error("Tidak bisa membuka URI: $uri")

    val outFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")

    inputStream.use { input ->
        FileOutputStream(outFile).use { output ->
            val buffer = ByteArray(8 * 1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
        }
    }
    return outFile
}
