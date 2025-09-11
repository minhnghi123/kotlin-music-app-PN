package com.example.musicapp

import android.content.Intent
import com.example.musicapp.receiver.MusicActions
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import com.example.musicapp.receiver.MusicService
import com.example.musicapp.ui.auth.LoginFragment
import com.example.musicapp.ui.home.HomeFragment
import com.example.musicapp.ui.library.LibraryFragment
import com.example.musicapp.ui.player.MiniPlayerFragment
import com.example.musicapp.ui.player.PlayerViewModel
import com.example.musicapp.ui.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private val playerVM: PlayerViewModel by viewModels()

    private fun applyUiFor(fragment: Fragment?) {
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        when (fragment) {
            is HomeFragment -> {
                bottom.visibility = View.VISIBLE
            }
            is LoginFragment, is com.example.musicapp.ui.auth.RegisterFragment -> {
                bottom.visibility = View.GONE
            }
            is SearchFragment -> {
                bottom.visibility = View.VISIBLE
            }
            else -> {
                bottom.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            applyUiFor(current)
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), "HOME")
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.miniPlayerContainer, MiniPlayerFragment())
            .commit()

        playerVM.currentSong.observe(this) { song ->
            findViewById<View>(R.id.miniPlayerContainer).visibility =
                if (song != null) View.VISIBLE else View.GONE
        }

        setupBottomNav()
    }

    /**
     * Hàm load fragment vào container chính
     */
    private fun loadFragment(fragment: Fragment, tag: String, addToBackStack: Boolean = false) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment != null && currentFragment::class == fragment::class) {
            return
        }

        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,  // giống Activity mở mới
                R.anim.slide_out_left,
                R.anim.slide_in_left,   // khi back
                R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, fragment, tag)

        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }

        transaction.commit()
    }

    /**
     * Setup bottom navigation (Home - Search - Library)
     */
    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment(), "HOME")
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment(), "SEARCH", addToBackStack = true)
                    true
                }
                R.id.nav_library -> {
                    loadFragment(LibraryFragment(), "LIBRARY", addToBackStack = true)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Hàm public cho Adapter/Fragment gọi để phát nhạc
     */
    fun showMiniPlayer(song: Song) {
        playerVM.play(song)
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicActions.ACTION_PLAY
            putExtra("SONG_TITLE", song.title)
            putExtra("SONG_ARTIST", song.artist.fullName)
            putExtra("SONG_URL", song.fileUrl)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    /**
     * Hàm public để LoginFragment gọi khi đăng nhập thành công
     */
    fun onLoginSuccess() {
        // về lại màn trước (thường là Home)
        supportFragmentManager.popBackStack()
    }

}
