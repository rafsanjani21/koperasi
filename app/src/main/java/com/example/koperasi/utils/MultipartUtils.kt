package com.example.koperasi.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

fun textPart(value: String): RequestBody =
    value.toRequestBody("text/plain".toMediaType())

fun filePart(context: Context, uri: Uri, fieldName: String): MultipartBody.Part {
    val file = uriToTempFile(context, uri)
    val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(fieldName, file.name, reqBody)
}

private fun uriToTempFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: error("Tidak bisa buka uri: $uri")

    val outFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
    FileOutputStream(outFile).use { out ->
        inputStream.use { input ->
            val buf = ByteArray(8 * 1024)
            while (true) {
                val r = input.read(buf)
                if (r <= 0) break
                out.write(buf, 0, r)
            }
        }
    }
    return outFile
}
