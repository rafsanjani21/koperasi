package com.example.koperasi.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.koperasi.data.remote.PaymentApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class PaymentRepositoryImpl(
    private val api: PaymentApi
) : PaymentRepository {

    override suspend fun getQr(): Bitmap? {
        return withContext(Dispatchers.IO) {
            val response = api.getQr()
            if (response.isSuccessful) {
                response.body()?.qrBase64?.let {
                    base64ToBitmap(it)
                }
            } else null
        }
    }

    override fun saveQrToGallery(context: Context, bitmap: Bitmap) {
        // kode save bitmap ke gallery
    }

    override suspend fun uploadBukti(
        context: Context,
        uri: Uri
    ): Result<Boolean> {

        return withContext(Dispatchers.IO) {
            try {
                val file = uriToFile(context, uri)

                val requestFile =
                    file.asRequestBody("image/*".toMediaTypeOrNull())

                val body =
                    MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        requestFile
                    )

                val response = api.uploadBukti(body)

                if (response.isSuccessful)
                    Result.success(true)
                else
                    Result.failure(Exception("Upload gagal"))

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun base64ToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun uriToFile(context: Context, uri: Uri): File {

        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(
            context.cacheDir,
            "upload_${System.currentTimeMillis()}.jpg"
        )

        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return file
    }
}