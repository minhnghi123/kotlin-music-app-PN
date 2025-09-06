package com.example.musicapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.ui.home.HomeFragment
import com.example.musicapp.ui.player.MiniPlayerFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.musicapp.ui.search.SearchFragment
import android.view.View
import androidx.activity.viewModels
import com.example.musicapp.models.Song
import com.example.musicapp.ui.player.PlayerViewModel

class MainActivity : AppCompatActivity() {
    private val playerVM: PlayerViewModel by viewModels()
    fun showMiniPlayer(song: Song) {
        playerVM.play(song)  // gọi ExoPlayer phát nhạc
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Load HomeFragment mặc định
        loadFragment(HomeFragment())
        // Load MiniPlayerFragment mặc định
        supportFragmentManager.beginTransaction()
            .replace(R.id.miniPlayerContainer, MiniPlayerFragment())
            .commit()

        // hiện/ẩn mini player
        playerVM.currentSong.observe(this) { song ->
            findViewById<View>(R.id.miniPlayerContainer).visibility =
                if (song != null) View.VISIBLE else View.GONE
        }


        // Xử lý Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    findViewById<View>(R.id.tvTitle).visibility = View.VISIBLE
                    findViewById<View>(R.id.btnLogin).visibility = View.VISIBLE
                    findViewById<View>(R.id.imgBanner).visibility = View.VISIBLE
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    findViewById<View>(R.id.tvTitle).visibility = View.GONE
                    findViewById<View>(R.id.btnLogin).visibility = View.GONE
                    findViewById<View>(R.id.imgBanner).visibility = View.GONE
                    loadFragment(SearchFragment())
                    true
                }
                R.id.nav_library -> {
                    true
                }
                else -> false
            }
        }

        // Xử lý nút Login
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
