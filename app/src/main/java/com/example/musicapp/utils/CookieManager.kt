package com.example.musicapp.utils

import android.content.Context
import android.content.SharedPreferences

class CookieManager(context: Context) {
//    quan ly cookie su dung SharedPreferences
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    fun saveCookie(cookie: String) {
        prefs.edit().putString("cookie", cookie).apply()
    }

    fun getCookie(): String? {
        return prefs.getString("cookie", null)
    }

    fun clearCookie() {
        prefs.edit().remove("cookie").apply()
    }
}