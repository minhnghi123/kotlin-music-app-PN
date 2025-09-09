package com.example.musicapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.auth.LoginFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: SongAdapter
    private lateinit var viewModel: SongViewModel
    private val playerVM: com.example.musicapp.ui.player.PlayerViewModel by activityViewModels()

    // Thêm các view cho UI Home
    private var btnLogin: Button? = null
    private var tvTitle: TextView? = null
    private var imgBanner: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.rvPlaylists)
        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = SongAdapter(emptyList()) { song ->
            playerVM.play(song)
        }
        rv.adapter = adapter

        // ViewModel
        viewModel = ViewModelProvider(this)[SongViewModel::class.java]
        viewModel.songs.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
        }
        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        // Load data
        viewModel.loadSongs()

        // --- Thêm logic cho btnLogin, title, banner ---
        btnLogin = view.findViewById(R.id.btnLogin)
        tvTitle = view.findViewById(R.id.tvTitle)
        imgBanner = view.findViewById(R.id.imgBanner)

        // Hiện các view này (nếu cần)
        tvTitle?.visibility = View.VISIBLE
        btnLogin?.visibility = View.VISIBLE
        imgBanner?.visibility = View.VISIBLE

        // Cập nhật text cho btnLogin
        updateLoginButtonText()

        btnLogin?.setOnClickListener {
            if (ApiClient.cookieManager?.getCookie() != null) {
                // Đã login -> logout
                ApiClient.api.logout().enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Đăng xuất thành công!",
                                Toast.LENGTH_SHORT
                            ).show()
                            ApiClient.cookieManager?.clearCookie()
                            updateLoginButtonText()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            "Lỗi: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                // Chưa login -> mở LoginFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, LoginFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun updateLoginButtonText() {
        btnLogin?.text = if (ApiClient.cookieManager?.getCookie() != null) "Đăng xuất" else "Đăng nhập"
    }

}
