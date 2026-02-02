package com.example.koperasi.data.mapper

import com.example.koperasi.pages.CompleteProfileForm

// ===== GENDER =====
fun genderLabel(value: String): String =
    when (value) {
        "L" -> "Laki-laki"
        "P" -> "Perempuan"
        else -> "-"
    }

// ===== DATE =====
// input  : dd-MM-yyyy
// output : yyyy-MM-dd
fun ddMmYyyyToIso(date: String): String {
    val parts = date.split("-")
    require(parts.size == 3) { "Format harus dd-MM-yyyy" }

    return "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
}

// ===== ADDRESS =====
fun buildAlamat(form: CompleteProfileForm): String =
    listOf(
        form.alamat,
        if (form.rt.isNotBlank() && form.rw.isNotBlank())
            "RT ${form.rt}/RW ${form.rw}" else "",
        form.kelurahan,
        form.kecamatan,
        form.kabupaten,
        form.provinsi
    ).filter { it.isNotBlank() }
        .joinToString(", ")
