package com.example.koperasi.ocr

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.koperasi.model.Ktp

class OCRforEKTPLibrary {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @SuppressLint("UnsafeOptInUsageError")
    suspend fun scanEKTP(image: Bitmap): Ktp {
        return withContext(Dispatchers.Default) {
            val imageSource = InputImage.fromBitmap(image, 0)
            val task = textRecognizer.process(imageSource)
            val result = Tasks.await(task)
            result.extractEktp() // parsing ala repo
        }
    }
}
