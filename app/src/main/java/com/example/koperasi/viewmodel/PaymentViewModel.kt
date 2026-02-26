package com.example.koperasi.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koperasi.repository.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val repository: PaymentRepository
) : ViewModel() {

    var qrBitmap: Bitmap? = null
        private set

    var uploadSuccess: Boolean = false
        private set

    var isLoading: Boolean = false
        private set

    private var context: Context? = null

    fun setContext(ctx: Context) {
        context = ctx
    }

    fun loadQr() {
        viewModelScope.launch {
            qrBitmap = repository.getQr()
        }
    }

    fun uploadBukti(uri: Uri) {
        val ctx = context ?: return

        viewModelScope.launch {
            isLoading = true

            val result = repository.uploadBukti(ctx, uri)

            uploadSuccess = result.isSuccess
            isLoading = false
        }
    }

    fun downloadQr() {
        val ctx = context ?: return
        val bitmap = qrBitmap ?: return

        repository.saveQrToGallery(ctx, bitmap)
    }
}