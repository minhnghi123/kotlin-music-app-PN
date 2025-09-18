package com.example.musicapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.playlists.AddToPlaylistRequest
import com.example.musicapp.models.playlists.CreatePlaylistRequest
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.auth.LoginFragment
import com.example.musicapp.ui.playlists.PlaylistAdapter
import kotlinx.coroutines.launch
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
        adapter.setOnAddToPlaylistClickListener { song ->
            showPlaylistDialog(song)
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
    private fun showPlaylistDialog(song: Song) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_playlists, null)
        val rvPlaylists = dialogView.findViewById<RecyclerView>(R.id.rvPlaylists)
        val btnCreatePlaylist = dialogView.findViewById<Button>(R.id.btnCreatePlaylist)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Chọn playlist")
            .setView(dialogView)
            .setNegativeButton("Đóng", null)
            .create()

        // Load danh sách playlist
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.getMyPlaylists()
                val playlists = response.data

                val adapter = PlaylistAdapter(playlists)
                rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
                rvPlaylists.adapter = adapter

                adapter.setOnItemClickListener { playlist ->
                    lifecycleScope.launch {
                        try {
                            val body = AddToPlaylistRequest(
                                 playlist._id,
                                 song._id
                            )
                            val addResponse = ApiClient.api.addToPlaylist(body)
                            if (addResponse.isSuccessful) {
                                val result = addResponse.body() ;
                                if (result != null) {
                                    if (result.success) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Đã thêm vào ${playlist.title}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            result.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            } else {
                                Toast.makeText(requireContext(),
                                    "Thêm thất bại", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(),
                                "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Xử lý nút tạo playlist ---
        btnCreatePlaylist.setOnClickListener {
            showCreatePlaylistDialog(song) {
                lifecycleScope.launch {
                    try {
                        val response = ApiClient.api.getMyPlaylists()
                        (rvPlaylists.adapter as? PlaylistAdapter)?.apply {
                            val newAdapter = PlaylistAdapter(response.data)
                            rvPlaylists.adapter = newAdapter
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        dialog.show()
    }
    private fun showCreatePlaylistDialog(song: Song, onCreated: () -> Unit) {
        val inputView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)
        val etTitle = inputView.findViewById<EditText>(R.id.etTitle)
        val etDescription = inputView.findViewById<EditText>(R.id.etDescription)

        AlertDialog.Builder(requireContext())
            .setTitle("Tạo Playlist mới")
            .setView(inputView)
            .setPositiveButton("Tạo") { d, _ ->
                val title = etTitle.text.toString().trim()
                val desc = etDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    lifecycleScope.launch {
                        try {
                            val body = CreatePlaylistRequest(
                                title = title,
                                description = desc,
                                songs = listOf(song._id) ,
                                song.coverImage
                            )
                            val response = ApiClient.api.createPlaylist(body)
                            if (response.code == "success") {
                                Toast.makeText(requireContext(),
                                    "Tạo playlist thành công!", Toast.LENGTH_SHORT).show()
                                onCreated()
                            } else {
                                Toast.makeText(requireContext(),
                                    "Không thể tạo playlist", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(),
                                "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show() ;
//
                            e.printStackTrace()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(),
                        "Tên playlist không được trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateLoginButtonText() {
        btnLogin?.text = if (ApiClient.cookieManager?.getCookie() != null) "Đăng xuất" else "Đăng nhập"
    }

}
