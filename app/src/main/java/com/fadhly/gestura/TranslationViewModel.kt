package com.fadhly.gestura

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TranslationViewModel(private val repository: TranslationRepository) : ViewModel() {

    val translatedText: LiveData<String> = repository.translatedText

    fun translateSignLanguageFrame(bitmap: Bitmap) {
        Log.d("TranslationViewModel", "Preparing to send frame to repository")
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("TranslationViewModel", "Sending frame to repository for translation")
            repository.sendFrame(bitmap)
        }
    }
}