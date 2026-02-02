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

// ================= TEXT =================
fun String.toTextBody(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())

// ================= FILE =================
fun filePart(
    context: Context,
    uri: Uri,
    fieldName: String
): MultipartBody.Part {

    val inputStream = context.contentResolver.openInputStream(uri)
        ?: error("Cannot open uri")

    val tempFile = File(
        context.cacheDir,
        "upload_${System.currentTimeMillis()}.jpg"
    )

    tempFile.outputStream().use { output ->
        inputStream.use { input ->
            input.copyTo(output)
        }
    }

    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
    val body = tempFile.asRequestBody(mime.toMediaType())

    return MultipartBody.Part.createFormData(
        fieldName,
        tempFile.name,
        body
    )
}





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
