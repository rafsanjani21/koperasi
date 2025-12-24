package com.example.koperasi.ocr

import com.google.mlkit.vision.text.Text
import com.example.koperasi.model.Ktp

/**
 * Ini versi ringkas dari repo: fokus ke NIK + Nama.
 * Prinsipnya sama: cari line yang startWith("NIK") dan "Nama" + cari inline line di bounding box yang sejajar.
 */

fun Text.extractEktp(): Ktp {
    val ktp = Ktp()

    var previousLine: Text.Line? = null

    textBlocks.forEach { block ->
        block.lines.forEach { line ->
            when {
                line.text.startsWith("NIK", ignoreCase = true) -> {
                    ktp.confidence++
                    val nik = findAndClean(line, "NIK")
                        ?.filterAlphabetToNumber()
                        ?.onlyDigits()
                    // pastikan 16 digit
                    ktp.nik = nik?.takeIf { it.length == 16 }
                    if (ktp.nik != null) ktp.confidence++
                }

                line.text.startsWith("Nama", ignoreCase = true) -> {
                    ktp.confidence++
                    val nama = findAndClean(line, "Nama")
                        ?.filterNumberToAlphabet()
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                    ktp.nama = nama
                    if (ktp.nama != null) ktp.confidence++
                }

                else -> {
                    // tambahan kecil: kadang nama lanjut ke baris bawah (ALL CAPS)
                    // ikuti pola repo: kalau previousLine adalah "Nama" dan baris berikutnya uppercase tanpa angka, append
                    val prev = previousLine
                    if (prev != null) {
                        val prevIsNama = prev.text.startsWith("Nama", ignoreCase = true)
                        if (prevIsNama && ktp.nama != null) {
                            val cur = line.text.trim()
                            if (cur.isNotBlank() && cur.none { it.isDigit() } && cur == cur.uppercase()) {
                                ktp.nama = (ktp.nama + " " + cur).trim()
                            }
                        }
                    }
                }
            }
            previousLine = line
        }
    }
    return ktp
}

fun Text.findAndClean(line: Text.Line, key: String): String? {
    return if (line.elements.size > key.split(" ").size) {
        line.text.cleanse(key)
    } else {
        findInline(line)?.text?.cleanse(key)
    }
}

fun Text.findInline(line: Text.Line): Text.Line? {
    val top = line.boundingBox?.top ?: return null
    val bottom = line.boundingBox?.bottom ?: return null
    val result = mutableListOf<Text.Line>()

    textBlocks.forEach { block ->
        block.lines.forEach { candidate ->
            val cy = candidate.boundingBox?.centerY()
            if (cy != null && cy in top..bottom && candidate.text != line.text) {
                result.add(candidate)
            }
        }
    }
    return result.minByOrNull { it.boundingBox?.left ?: Int.MAX_VALUE }
}

fun String.cleanse(text: String, ignoreCase: Boolean = true): String {
    return replace(text, "", ignoreCase).replace(":", "").trim()
}

fun String.filterNumberToAlphabet(): String {
    return replace("0", "O")
        .replace("1", "I")
        .replace("4", "A")
        .replace("5", "S")
        .replace("7", "T")
        .replace("8", "B")
}

fun String.filterAlphabetToNumber(): String {
    return replace("O", "0")
        .replace("I", "1")
        .replace("l", "1")
        .replace("A", "4")
        .replace("S", "5")
        .replace("T", "7")
        .replace("B", "8")
}

fun String.onlyDigits(): String = filter { it.isDigit() }
