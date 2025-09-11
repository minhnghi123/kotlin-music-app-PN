package com.example.musicapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class MusicControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        println("MusicControlReceiver received action: $action")

        val serviceIntent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }
        // Gửi action về MusicService xử lý
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
