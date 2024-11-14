package com.fadhly.gestura

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class TranslationRepository(private val apiService: ApiService) {

    private val _translatedText = MutableLiveData<String>()
    val translatedText: LiveData<String> get() = _translatedText

    fun sendFrame(bitmap: Bitmap) {
        Log.d("TranslationRepository", "Converting bitmap to byte array")
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()

        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
        val framePart = MultipartBody.Part.createFormData("frame", "frame.jpg", requestBody)

        Log.d("TranslationRepository", "Sending frame to API for translation")
        apiService.translateFrame(framePart).enqueue(object : Callback<TranslationResponse> {
            override fun onResponse(
                call: Call<TranslationResponse>,
                response: Response<TranslationResponse>
            ) {
                if (response.isSuccessful) {
                    val translatedText = response.body()?.translatedText ?: "Translation failed"
                    Log.d("TranslationRepository", "Received translated text: $translatedText")
                    _translatedText.value = translatedText
                } else {
                    Log.e("TranslationRepository", "Translation failed with response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                Log.e("TranslationRepository", "Failed to translate frame: ${t.message}")
                _translatedText.value = "Error: ${t.message}"
            }
        })
    }
}