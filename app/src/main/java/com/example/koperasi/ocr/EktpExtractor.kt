package com.example.koperasi.ocr

import com.google.mlkit.vision.text.Text
import com.example.koperasi.model.Ktp

/**
 * Ini versi ringkas dari repo: fokus ke NIK + Nama.
 * Prinsipnya sama: cari line yang startWith("NIK") dan "Nama" + cari inline line di bounding box yang sejajar.
 */

/**
 * Extension function untuk mengekstrak data e-KTP dari hasil OCR ML Kit.
 *
 * Data yang diekstrak:
 * - NIK (16 digit)
 * - Nama
 *
 * Pendekatan:
 * - Mencari baris yang diawali "NIK" dan "Nama"
 * - Melakukan koreksi kesalahan OCR (huruf ↔ angka)
 * - Menggunakan bounding box untuk mencari teks inline (sejajar)
 * - Menambahkan confidence score berdasarkan keberhasilan ekstraksi
 *
 * @return objek [Ktp] berisi data hasil ekstraksi dan confidence
 */
fun Text.extractEktp(): Ktp {
    val ktp = Ktp()

    // Menyimpan baris sebelumnya (untuk kasus nama multi-baris)
    var previousLine: Text.Line? = null

    textBlocks.forEach { block ->
        block.lines.forEach { line ->
            when {
                /**
                 * Deteksi baris NIK
                 */
                line.text.startsWith("NIK", ignoreCase = true) -> {
                    ktp.confidence++
                    val nik = findAndClean(line, "NIK")
                        ?.filterAlphabetToNumber() // OCR correction
                        ?.onlyDigits()  // hanya angka
                    // pastikan 16 digit
                    ktp.nik = nik?.takeIf { it.length == 16 }
                    if (ktp.nik != null) ktp.confidence++
                }

                /**
                 * Deteksi baris Nama
                 */
                line.text.startsWith("Nama", ignoreCase = true) -> {
                    ktp.confidence++
                    val nama = findAndClean(line, "Nama")
                        ?.filterNumberToAlphabet() // OCR correction
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                    ktp.nama = nama
                    if (ktp.nama != null) ktp.confidence++
                }

                /**
                 * Kasus tambahan:
                 * Nama sering lanjut ke baris bawah (huruf kapital semua)
                 */
                else -> {
                    // tambahan kecil: kadang nama lanjut ke baris bawah (ALL CAPS)
                    // ikuti pola repo: kalau previousLine adalah "Nama" dan baris berikutnya uppercase tanpa angka, append
                    val prev = previousLine
                    if (prev != null) {
                        val prevIsNama = prev.text.startsWith("Nama", ignoreCase = true)
                        if (prevIsNama && ktp.nama != null) {
                            val cur = line.text.trim()
                            // Valid jika:
                            // - tidak kosong
                            // - tidak mengandung angka
                            // - huruf kapital semua
                            if (cur.isNotBlank() && cur.none { it.isDigit() } && cur == cur.uppercase()) {
                                ktp.nama = (ktp.nama + " " + cur).trim()
                            }
                        }
                    }
                }
            }
            // Simpan baris saat ini sebagai previousLine
            previousLine = line
        }
    }
    return ktp
}

/**
 * Membersihkan teks hasil OCR dengan menghapus keyword tertentu.
 *
 * Jika teks tidak cukup panjang, akan mencoba mencari teks inline
 * (sejajar secara horizontal).
 *
 * @param line baris OCR utama
 * @param key keyword seperti "NIK" atau "Nama"
 * @return teks yang sudah dibersihkan atau null
 */
fun Text.findAndClean(line: Text.Line, key: String): String? {
    return if (line.elements.size > key.split(" ").size) {
        line.text.cleanse(key)
    } else {
        findInline(line)?.text?.cleanse(key)
    }
}

/**
 * Mencari teks inline (sejajar secara vertikal) berdasarkan bounding box.
 *
 * Digunakan saat teks utama hanya berisi keyword,
 * sedangkan nilainya berada di sebelah kanan.
 *
 * @param line baris referensi
 * @return baris OCR terdekat secara horizontal
 */
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
    // Ambil teks paling kiri (paling dekat dengan keyword)
    return result.minByOrNull { it.boundingBox?.left ?: Int.MAX_VALUE }
}

/**
 * Menghapus keyword dan karakter ":" dari teks OCR.
 *
 * @param text keyword yang akan dihapus
 * @param ignoreCase apakah case-insensitive
 */
fun String.cleanse(text: String, ignoreCase: Boolean = true): String {
    return replace(text, "", ignoreCase).replace(":", "").trim()
}

/**
 * Koreksi OCR:
 * Mengubah angka yang sering salah terbaca menjadi huruf.
 *
 * Contoh:
 * 0 → O, 1 → I, 4 → A
 */
fun String.filterNumberToAlphabet(): String {
    return replace("0", "O")
        .replace("1", "I")
        .replace("4", "A")
        .replace("5", "S")
        .replace("7", "T")
        .replace("8", "B")
}

/**
 * Koreksi OCR:
 * Mengubah huruf yang sering salah terbaca menjadi angka.
 *
 * Contoh:
 * O → 0, I → 1, A → 4
 */
fun String.filterAlphabetToNumber(): String {
    return replace("O", "0")
        .replace("I", "1")
        .replace("l", "1")
        .replace("A", "4")
        .replace("S", "5")
        .replace("T", "7")
        .replace("B", "8")
}

/**
 * Mengambil hanya karakter digit dari string.
 */
fun String.onlyDigits(): String = filter { it.isDigit() }
