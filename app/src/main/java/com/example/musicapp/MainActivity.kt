package com.example.musicapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.auth.LoginFragment
import com.example.musicapp.ui.home.HomeFragment
import com.example.musicapp.ui.library.LibraryFragment
import com.example.musicapp.ui.player.MiniPlayerFragment
import com.example.musicapp.ui.player.PlayerViewModel
import com.example.musicapp.ui.search.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val playerVM: PlayerViewModel by viewModels()
    private lateinit var btnLogin: Button

    private fun applyUiFor(fragment: Fragment?) {
        val title = findViewById<View>(R.id.tvTitle)
        val btn = findViewById<View>(R.id.btnLogin)
        val banner = findViewById<View>(R.id.imgBanner)
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        when (fragment) {
            is HomeFragment -> {
                title.visibility = View.VISIBLE
                btn.visibility = View.VISIBLE
                banner.visibility = View.VISIBLE
                bottom.visibility = View.VISIBLE
            }
            is LoginFragment, is com.example.musicapp.ui.auth.RegisterFragment -> {
                // Màn hình Auth: ẩn toàn bộ UI Home + bottom nav
                title.visibility = View.GONE
                btn.visibility = View.GONE
                banner.visibility = View.GONE
                bottom.visibility = View.GONE
            }
            is SearchFragment -> {
                // Màn hình Search: ẩn UI Home, giữ bottom nav
                title.visibility = View.GONE
                btn.visibility = View.GONE
                banner.visibility = View.GONE
                bottom.visibility = View.VISIBLE
            }
            else -> {
                // Mặc định: có bottom nav
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


        // Load MiniPlayerFragment mặc định
        supportFragmentManager.beginTransaction()
            .replace(R.id.miniPlayerContainer, MiniPlayerFragment())
            .commit()

        // Hiện/ẩn mini player dựa vào currentSong
        playerVM.currentSong.observe(this) { song ->
            findViewById<View>(R.id.miniPlayerContainer).visibility =
                if (song != null) View.VISIBLE else View.GONE
        }

        btnLogin = findViewById(R.id.btnLogin)

        setupBottomNav()
        setupLoginButton()
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
                    toggleHomeUI(true)
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
     * Ẩn/Hiện UI ở Home (banner, title, nút login)
     */
    private fun toggleHomeUI(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        findViewById<View>(R.id.tvTitle).visibility = visibility
        btnLogin.visibility = visibility
        findViewById<View>(R.id.imgBanner).visibility = visibility
    }

    /**
     * Setup nút Login/Logout
     */
    private fun setupLoginButton() {
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Khi mở app, cập nhật text theo trạng thái login
        if (ApiClient.cookieManager?.getCookie() != null) {
            btnLogin.text = "Đăng xuất"
        } else {
            btnLogin.text = "Đăng nhập"
        }

        btnLogin.setOnClickListener {
            if (ApiClient.cookieManager?.getCookie() != null) {
                // Đã login -> logout
                ApiClient.api.logout().enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@MainActivity,
                                "Đăng xuất thành công!",
                                Toast.LENGTH_SHORT
                            ).show()
                            ApiClient.cookieManager?.clearCookie()
                            btnLogin.text = "Đăng nhập"
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(
                            this@MainActivity,
                            "Lỗi: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                // Chưa login -> mở LoginFragment (không reload Home)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, LoginFragment())
                    .addToBackStack(null) // để back quay về Home
                    .commit()
            }
        }
    }

    private fun updateLoginButtonText() {
        if (ApiClient.cookieManager?.getCookie() != null) {
            btnLogin.text = "Đăng xuất"
        } else {
            btnLogin.text = "Đăng nhập"
        }
    }

    /**
     * Hàm public cho Adapter/Fragment gọi để phát nhạc
     */
    fun showMiniPlayer(song: Song) {
        playerVM.play(song)  // gọi ExoPlayer phát nhạc
    }

    /**
     * Hàm public để LoginFragment gọi khi đăng nhập thành công
     */
    fun onLoginSuccess() {
        // về lại màn trước (thường là Home)
        supportFragmentManager.popBackStack()
        // applyUiFor sẽ chạy lại nhờ listener, không cần ẩn/hiện thủ công
        findViewById<Button>(R.id.btnLogin).text = "Đăng xuất"
    }
}
