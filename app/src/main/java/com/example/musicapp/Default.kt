package com.example.musicapp

import android.app.Application
import com.example.musicapp.network.ApiClient

class Default: Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this) // Khởi tạo CookieManager 1 lần duy nhất
    }
}