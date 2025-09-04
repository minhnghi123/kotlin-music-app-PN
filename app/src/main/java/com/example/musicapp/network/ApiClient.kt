package com.example.musicapp.network
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
//    how to use buildConfig
    private const val BASE_URL = "http://10.0.2.2:3000/"
    private val logging by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }
    private val okHttp by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }1
}