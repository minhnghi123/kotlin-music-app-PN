package com.example.musicapp.network

import android.content.Context
import com.example.musicapp.models.artists.Artist
import com.example.musicapp.models.songs.ArtistDeserializer
import com.example.musicapp.models.songs.Song
import com.example.musicapp.models.songs.SongDeserializer
import com.example.musicapp.models.users.UserResponse
import com.example.musicapp.utils.CookieManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    var cookieManager: CookieManager? = null
    var currentUser: UserResponse? = null

    fun init(context: Context) {
        cookieManager = CookieManager(context.applicationContext)
    }

    // Interceptor để lưu cookie khi login
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

    // 👇 QUAN TRỌNG: Gson với ArtistDeserializer
    private val gson by lazy {
        android.util.Log.d("ApiClient", "=== Creating Gson with SongDeserializer ===")
        GsonBuilder()
            .registerTypeAdapter(Song::class.java, SongDeserializer())
            .setLenient()
            .create()
    }

    private val okHttp by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(receiveInterceptor)
            .addInterceptor(addInterceptor)
            .build()
    }

    // 👇 PHẢI dùng gson custom ở đây
    val api: ApiService by lazy {
        android.util.Log.d("ApiClient", "=== Creating ApiService with custom Gson ===")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson)) // 👈 Dùng gson custom
            .build()
            .create(ApiService::class.java)
    }

    // Hàm gọi API /user/me để lấy user hiện tại
    suspend fun fetchCurrentUser() {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getUserProfile().execute()
                if (response.isSuccessful) {
                    currentUser = response.body()
                } else {
                    currentUser = null
                }
            } catch (e: Exception) {
                currentUser = null
            }
        }
    }
}
