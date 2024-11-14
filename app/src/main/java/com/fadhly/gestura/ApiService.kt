package com.fadhly.gestura

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("translate-frame")
    fun translateFrame(
        @Part frame: MultipartBody.Part
    ): Call<TranslationResponse>
}

data class TranslationResponse(
    val translatedText: String
)