package com.example.koperasi.repository

import android.graphics.Bitmap
import com.example.koperasi.model.Ktp
import com.example.koperasi.ocr.OCRforEKTPLibrary

interface KTPRepository {
    suspend fun scanKTP(image: Bitmap): Ktp
}

class KTPRepositoryImpl(
    private val ocrLibrary: OCRforEKTPLibrary
) : KTPRepository {
    override suspend fun scanKTP(image: Bitmap): Ktp {
        return ocrLibrary.scanEKTP(image)
    }
}
