package com.example.musicapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.content.Intent
import android.widget.Toast
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.auth.SignUpRequest
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
//        lay cac thanh phan trong activity
        val etUsername = findViewById<EditText>(R.id.et_full_name_register)
        val etEmail = findViewById<EditText>(R.id.et_email_login)
        val etPassword = findViewById<EditText>(R.id.et_password_register)
        val etConfirm = findViewById<EditText>(R.id.et_confirm_password_register)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvGoToLogin: TextView = findViewById(R.id.tv_go_to_login)


//     xu ly su kien khi click vao nut register
        btnRegister.setOnClickListener {
//      Tao request tu cac gia tri nguoi dung nhap vao
            val request = SignUpRequest(
                username = etUsername.text.toString(),
                email = etEmail.text.toString(),
                password = etPassword.text.toString(),
                rePassword = etConfirm.text.toString()
            )
            ApiClient.api.signUp(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(this@SignUpActivity, body?.message, Toast.LENGTH_SHORT).show()
                        // TODO: chuyển sang LoginActivity nếu cần
                    } else {
                        Toast.makeText(this@SignUpActivity, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@SignUpActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        }



//       xu ly su kien khi click vao nut register
        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}