package com.example.koperasi.ocr

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koperasi.model.Ktp
import com.example.koperasi.repository.KTPRepository
import com.example.koperasi.utils.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OCRViewModel(
    private val ktpRepository: KTPRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OCRState())
    val uiState = _uiState.asStateFlow()

    fun processImage(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(bitmapState = ViewState.Loading, ktpState = ViewState.Idle) }

                val loadedBitmap = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }

                _uiState.update { it.copy(bitmapState = ViewState.Success(loadedBitmap)) }

                // optional: crop rasio KTP biar parsing lebih stabil (ini dari repo)
                val croppedBitmap = cropToKTPRatio(loadedBitmap)

                val result = ktpRepository.scanKTP(croppedBitmap)
                _uiState.update { it.copy(ktpState = ViewState.Success(result)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(bitmapState = ViewState.Error(e.message ?: "Terjadi Kesalahan")) }
            }
        }
    }

    fun reset() {
        _uiState.value = OCRState()
    }

    fun updateKTPModel(updatedModel: Ktp) {
        _uiState.update { it.copy(ktpState = ViewState.Success(updatedModel)) }
    }

    private fun cropToKTPRatio(bitmap: Bitmap): Bitmap {
        val targetRatio = 1.585f
        val width = bitmap.width
        val height = bitmap.height
        val currentRatio = width.toFloat() / height.toFloat()

        val (cropWidth, cropHeight) = if (currentRatio > targetRatio) {
            Pair((height * targetRatio).toInt(), height)
        } else {
            Pair(width, (width / targetRatio).toInt())
        }

        val startX = (width - cropWidth) / 2
        val startY = (height - cropHeight) / 2

        return Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight)
    }
}
