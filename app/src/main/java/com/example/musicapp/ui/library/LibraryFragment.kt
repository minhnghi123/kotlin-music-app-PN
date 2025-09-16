package com.example.musicapp.ui.library

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.users.UserResponse
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.artist.ArtistAdapter
import com.example.musicapp.ui.home.SongAdapter
import com.example.musicapp.ui.playlists.PlaylistAdapter
import com.example.musicapp.utils.PreferenceHelper
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class LibraryFragment : Fragment() {
    private lateinit var ivAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var rvSongs: RecyclerView
    private lateinit var rvArtists: RecyclerView
    private lateinit var btnLogout: Button
    private lateinit var  btnEditProfile: Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        val switchDarkMode = view.findViewById<Switch>(R.id.switchDarkMode)
        // Kiểm tra mode hiện tại
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        switchDarkMode.isChecked = (nightMode == AppCompatDelegate.MODE_NIGHT_YES)

        ivAvatar = view.findViewById(R.id.ivAvatar)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        rvPlaylists = view.findViewById(R.id.rvPlaylists)
        rvSongs = view.findViewById(R.id.rvSongs)
        rvArtists = view.findViewById(R.id.rvArtists)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        // setup RecyclerView horizontal
        rvPlaylists.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvArtists.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        fetchUserData()
//        click sự kiện chi tiết
        btnEditProfile.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragmentContainer,
                ProfileDetailFragment())
                .addToBackStack("PROFILE_DETAIL")
                .commit()
        }
        // Lắng nghe khi bật/tắt
        switchDarkMode.setOnClickListener {
            val newMode = !PreferenceHelper.isDarkMode(requireContext())
            PreferenceHelper.setDarkMode(requireContext(), newMode)
            PreferenceHelper.applyTheme(requireContext())

            // restart activity để áp dụng ngay
            activity?.recreate()
        }
        return view
    }

    private fun fetchUserData() {
        ApiClient.api.getUserProfile().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.data

                    // Gán thông tin cơ bản
                    tvUsername.text = user.username
                    tvEmail.text = user.email
                    Glide.with(requireContext()).load(user.avatar).into(ivAvatar)

                    // Gán danh sách playlist, songs, artists
                    rvPlaylists.adapter = PlaylistAdapter(user.playlist)
                    rvSongs.adapter = SongAdapter(user.follow_songs)
                    rvArtists.adapter = ArtistAdapter(user.follow_artists)

                } else {
                    Toast.makeText(requireContext(), "Lỗi load dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
