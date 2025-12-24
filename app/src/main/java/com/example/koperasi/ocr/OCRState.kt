package com.example.koperasi.ocr

import android.graphics.Bitmap
import com.example.koperasi.model.Ktp
import com.example.koperasi.utils.ViewState

data class OCRState(
    val ktpState: ViewState<Ktp> = ViewState.Idle,
    val bitmapState: ViewState<Bitmap> = ViewState.Idle
)
