package com.example.koperasi

import android.app.Application
import com.example.koperasi.data.remote.ApiClient

class KoperasiApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 🔥 INIT GLOBAL (WAJIB)
        ApiClient.init(this)
    }
}
