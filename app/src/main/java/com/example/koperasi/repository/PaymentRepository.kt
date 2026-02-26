package com.example.koperasi.repository

import android.net.Uri
import android.content.Context
import android.graphics.Bitmap

interface PaymentRepository {

    suspend fun getQr(): Bitmap?

    suspend fun uploadBukti(context: Context, uri: Uri): Result<Boolean>
    fun saveQrToGallery(context: Context, bitmap: Bitmap)
}