package com.example.koperasi.utils

import android.content.ContentValues // <-- ADD THIS
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore   // <-- ADD THIS
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.text.SimpleDateFormat  // <-- ADD THIS
import java.util.Locale

/**
 * A data class to hold the results of the OCR process.
 * @property rawText The full text recognized from the image.
 * @property nik The extracted 16-digit NIK, or null if not found.
 */
data class OcrResult(
    val rawText: String,
    val nik: String?,
)

/**
 * Runs OCR on an image specified by a URI to find a NIK.
 *
 * This is a suspend function and should be called from a coroutine.
 *
 * @param context The application context.
 * @param imageUri The URI of the image to process.
 * @return An [OcrResult] containing the raw text and the found NIK.
 * @throws IOException If the image cannot be processed from the URI.
 * @throws Exception For other ML Kit processing errors.
 */
suspend fun runNikOcrOnUri(context: Context, imageUri: Uri): OcrResult {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromFilePath(context, imageUri)

    // Await the result from the ML Kit Task API
    val visionText = recognizer.process(image).await()

    val rawText = visionText.text
    var foundNik: String? = null

    // Regex to find a 16-digit number
    val nikRegex = Regex("\\b\\d{16}\\b")

    // Search for the NIK in the recognized text blocks
    for (block in visionText.textBlocks) {
        val blockText = block.text.replace(" ", "").replace("\n", "")
        val matchResult = nikRegex.find(blockText)
        if (matchResult != null) {
            foundNik = matchResult.value
            break // Stop once the first valid NIK is found
        }
    }

    // As a fallback, search the entire raw text if not found in blocks
    if (foundNik == null) {
        val strippedRawText = rawText.replace(" ", "").replace("\n", "")
        nikRegex.find(strippedRawText)?.let {
            foundNik = it.value
        }
    }

    return OcrResult(rawText = rawText, nik = foundNik)
}

fun createImageUri(context: Context): Uri? {
    val contentResolver = context.contentResolver
    // The import for Locale was incorrect, it should be java.util.Locale
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(System.currentTimeMillis())
    val imageFileName = "JPEG_${timeStamp}_"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "$imageFileName.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        // Scoped storage for modern Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/KoperasiApp")
        }
    }

    // This returns a URI where the camera app can write the image.
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}
