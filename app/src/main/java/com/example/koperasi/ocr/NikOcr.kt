package com.example.koperasi.ocr

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * OCR hanya NIK dari foto KTP (Uri kamera).
 * Return: Pair(nik, rawText)
 */
suspend fun scanNikFromUri(context: Context, uri: Uri): Pair<String?, String> {
    return withContext(Dispatchers.Default) {
        val input = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = Tasks.await(recognizer.process(input))

        val nik = result.extractNikOnly_Unique()
        nik to (result.text ?: "")
    }
}

/**
 * Versi “repo-style” tapi helper dibuat unik agar tidak konflik dengan extractor lain.
 */
private fun Text.extractNikOnly_Unique(): String? {
    // 1) Cari baris "NIK"
    textBlocks.forEach { block ->
        block.lines.forEach { line ->
            if (line.text.startsWith("NIK", ignoreCase = true)) {
                val candidate = this.findAndClean_Unique(line, "NIK")
                    ?.filterAlphabetToNumber_Unique()
                    ?.onlyDigits_Unique()

                val nik = candidate?.takeIf { it.length >= 16 }?.take(16)
                if (nik != null) return nik
            }
        }
    }

    // 2) Fallback: cari 16 digit dari seluruh teks
    val digits = this.text
        .filterAlphabetToNumber_Unique()
        .onlyDigits_Unique()

    return digits.windowed(size = 16, step = 1, partialWindows = false)
        .firstOrNull { it.length == 16 }
}

private fun Text.findAndClean_Unique(line: Text.Line, key: String): String? {
    return if (line.elements.size > key.split(" ").size) {
        line.text.cleanse_Unique(key)
    } else {
        findInline_Unique(line)?.text?.cleanse_Unique(key)
    }
}

private fun Text.findInline_Unique(line: Text.Line): Text.Line? {
    val top = line.boundingBox?.top ?: return null
    val bottom = line.boundingBox?.bottom ?: return null

    val candidates = mutableListOf<Text.Line>()
    textBlocks.forEach { block ->
        block.lines.forEach { other ->
            val cy = other.boundingBox?.centerY()
            if (cy != null && cy in top..bottom && other.text != line.text) {
                candidates.add(other)
            }
        }
    }
    return candidates.minByOrNull { it.boundingBox?.left ?: Int.MAX_VALUE }
}

private fun String.cleanse_Unique(text: String, ignoreCase: Boolean = true): String =
    replace(text, "", ignoreCase).replace(":", "").trim()

private fun String.filterAlphabetToNumber_Unique(): String =
    replace("O", "0")
        .replace("I", "1")
        .replace("l", "1")
        .replace("A", "4")
        .replace("S", "5")
        .replace("T", "7")
        .replace("B", "8")

private fun String.onlyDigits_Unique(): String = filter { it.isDigit() }
