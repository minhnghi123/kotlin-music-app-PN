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
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.auth.LoginRequest
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username: EditText = view.findViewById(R.id.et_email_login)
        val password: EditText = view.findViewById(R.id.et_password_login)
        val btnLogin: Button = view.findViewById(R.id.btn_login)
        val tvGoToRegister: TextView = view.findViewById(R.id.tv_go_to_register)

        btnLogin.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            val request = LoginRequest(user, pass)
            ApiClient.api.login(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Toast.makeText(requireContext(), body?.message, Toast.LENGTH_SHORT).show()

                        (activity as? MainActivity)?.onLoginSuccess()
                    } else {
                        Toast.makeText(requireContext(), "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        tvGoToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
