package com.example.koperasi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.koperasi.repository.PaymentRepository

class PaymentViewModelFactory(
    private val repository: PaymentRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PaymentViewModel(repository) as T
    }
}