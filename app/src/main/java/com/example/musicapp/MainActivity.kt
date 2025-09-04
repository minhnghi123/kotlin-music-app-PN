package com.example.musicapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Load HomeFragment mặc định
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()
//        Xu ly mini player
        supportFragmentManager.beginTransaction()
            .replace(R.id.miniPlayerContainer, com.example.musicapp.ui.player.MiniPlayerFragment())
            .commit()


//        Xu ly Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_search -> {
                    // TODO: replace with SearchFragment()
                    true
                }
                R.id.nav_library -> {
                    // TODO: replace with LibraryFragment()
                    true
                }
                else -> false
            }
        }




        // Xử lý nút Login / Logout
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        // Khi mở app, set text phù hợp
        if (ApiClient.cookieManager?.getCookie() != null) {
            btnLogin.text = "Đăng xuất"
        } else {
            btnLogin.text = "Đăng nhập"
        }
        Toast.makeText(this@MainActivity, " ${ApiClient.cookieManager?.getCookie().toString()}", Toast.LENGTH_SHORT).show()
        btnLogin.setOnClickListener {
            if (ApiClient.cookieManager?.getCookie() != null) {
                // Đã login -> logout
                ApiClient.api.logout().enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
                            ApiClient.cookieManager?.clearCookie()
                            btnLogin.text = "Đăng nhập"
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // Chưa login -> mở LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

}