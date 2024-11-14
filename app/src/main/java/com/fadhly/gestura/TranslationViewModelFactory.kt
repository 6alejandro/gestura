package com.fadhly.gestura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TranslationViewModelFactory(
    private val repository: TranslationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslationViewModel::class.java)) {
            return TranslationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}