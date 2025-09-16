package com.example.musicapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object PreferenceHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LOGGED_IN = "isLoggedIn"
    private const val KEY_DARK_MODE = "darkMode"

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOGGED_IN, false)
    }

    fun setLoggedIn(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()
    }

    // ✅ Dark Mode
    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
    }

    fun applyTheme(context: Context) {
        val darkMode = isDarkMode(context)
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
