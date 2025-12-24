package com.example.koperasi.wilayah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WilayahViewModelFactory(
    private val repo: WilayahRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WilayahViewModel(repo) as T
    }
}
