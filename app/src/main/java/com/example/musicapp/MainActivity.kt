package com.example.musicapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//    Set up  RecyclerView
        val rvPlaylists = findViewById<RecyclerView>(R.id.rvPlaylists) ;
        rvPlaylists.layoutManager =  LinearLayoutManager(this ) ;
        rvPlaylists.adapter =  PlaylistAdapter(getSampleData()) ;
//    Set up  BottomNavigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav) ;
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // TODO: load fragment home
                    true
                }
                R.id.nav_search -> {
                    // TODO: load fragment search
                    true
                }
                R.id.nav_library -> {
                    // TODO: load fragment library
                    true
                }
                else -> false
            }
        }

    }
    private fun getSampleData(): List<String> {
        return listOf("Top Hits", "Chill Vibes", "Workout Mix", "Love Songs", "Vietnamese Hits")
    }
}