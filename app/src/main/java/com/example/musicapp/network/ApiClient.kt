package com.example.musicapp.network
import android.content.Context
import com.example.musicapp.utils.CookieManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
//    how to use buildConfig
    private const val BASE_URL = "https://api-be-music-2.onrender.com/" ;
    public var cookieManager: CookieManager? =null ;
    fun init(context: Context) {
        cookieManager = CookieManager(context.applicationContext)
    }
//    Interceptor de luu cookie khi login
    private val receiveInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val cookies = response.headers("Set-Cookie")
        if (cookies.isNotEmpty()) {
            cookieManager?.saveCookie(cookies[0])
        }
        response
    }
    // Interceptor để gắn cookie vào request
    private val addInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        cookieManager?.getCookie()?.let {
            requestBuilder.addHeader("Cookie", it)
        }
        chain.proceed(requestBuilder.build())
    }
    private val logging by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }
    private val okHttp by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(receiveInterceptor)
            .addInterceptor(addInterceptor)
            .build()
    }
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}