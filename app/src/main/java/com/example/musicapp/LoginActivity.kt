package com.example.musicapp

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.auth.LoginRequest
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val username: EditText =  findViewById(R.id.et_email_login) ;
        val password: EditText  = findViewById(R.id.et_password_login) ;
        val btnLogin: Button   =  findViewById(R.id.btn_login) ;
        val tvGoToRegister: TextView = findViewById(R.id.tv_go_to_register)
//Xu ly su kien khi click vao nut login
        btnLogin.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            val request = LoginRequest(user,pass) ;
            ApiClient.api.login(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(this@LoginActivity, body?.message, Toast.LENGTH_SHORT).show()
                        // TODO: lưu token/cookie vào SharedPreferences
                        val intent = Intent(this@LoginActivity, MainActivity::class.java) ;
                        startActivity(intent) ;
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

// xu ly su kien khi click toRegister
        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}