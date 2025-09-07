package com.example.musicapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.musicapp.R
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.auth.SignUpRequest
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsername = view.findViewById<EditText>(R.id.et_full_name_register)
        val etEmail = view.findViewById<EditText>(R.id.et_email_login)
        val etPassword = view.findViewById<EditText>(R.id.et_password_register)
        val etConfirm = view.findViewById<EditText>(R.id.et_confirm_password_register)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val tvGoToLogin: TextView = view.findViewById(R.id.tv_go_to_login)

        btnRegister.setOnClickListener {
            val request = SignUpRequest(
                username = etUsername.text.toString(),
                email = etEmail.text.toString(),
                password = etPassword.text.toString(),
                rePassword = etConfirm.text.toString()
            )

            ApiClient.api.signUp(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(requireContext(), body?.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Đăng ký thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        tvGoToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
