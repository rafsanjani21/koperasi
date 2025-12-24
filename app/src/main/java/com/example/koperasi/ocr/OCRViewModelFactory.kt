package com.example.koperasi.ocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.koperasi.repository.KTPRepository

class OCRViewModelFactory(
    private val ktpRepository: KTPRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OCRViewModel(ktpRepository) as T
    }
}
