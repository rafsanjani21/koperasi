package com.example.koperasi

import android.app.Application
import com.example.koperasi.data.remote.ApiClient


/**
 * Kelas Application utama untuk aplikasi Koperasi.
 *
 * Kelas ini dieksekusi pertama kali saat aplikasi dibuat,
 * sebelum Activity atau Service mana pun dijalankan.
 *
 * Digunakan untuk:
 * - Inisialisasi komponen global aplikasi
 * - Setup dependency yang membutuhkan Context aplikasi
 * - Menjamin resource siap digunakan di seluruh lifecycle app
 *
 * Pastikan class ini didaftarkan di AndroidManifest.xml:
 * android:name=".KoperasiApp"
 */
class KoperasiApp : Application() {

    /**
     * Dipanggil satu kali saat aplikasi pertama kali dijalankan.
     * Cocok untuk inisialisasi global.
     */
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi ApiClient (Retrofit, interceptor, dll)
        // Wajib dipanggil sebelum repository digunakan
        ApiClient.init(this)
    }
}
