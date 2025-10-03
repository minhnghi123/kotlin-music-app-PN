package com.example.musicapp.ui.library

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.models.users.UserResponse
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.artist.ArtistAdapter
import com.example.musicapp.ui.home.SongAdapter
import com.example.musicapp.ui.playlists.PlaylistAdapter
import com.example.musicapp.ui.playlists.PlaylistDetailFragment
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.utils.PreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibraryFragment : Fragment() {

    private lateinit var rvPlaylists: RecyclerView
    private lateinit var rvSongs: RecyclerView
    private lateinit var rvFavoriteSongs: RecyclerView
    private lateinit var rvArtists: RecyclerView
    private lateinit var favoriteRepository: FavoriteSongsRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        val switchDarkMode = view.findViewById<Switch>(R.id.switchDarkMode)
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        switchDarkMode.isChecked = (nightMode == AppCompatDelegate.MODE_NIGHT_YES)

        rvPlaylists = view.findViewById(R.id.rvPlaylists)
        rvSongs = view.findViewById(R.id.rvSongs)
        rvFavoriteSongs = view.findViewById(R.id.rvFavoriteSongs)
        rvArtists = view.findViewById(R.id.rvArtists)

        // setup RecyclerView horizontal
        rvPlaylists.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvFavoriteSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvArtists.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Initialize repository
        favoriteRepository = FavoriteSongsRepository()

        fetchUserData()
        loadFavoriteSongs()

        view.findViewById<TextView>(R.id.tvFavHeader).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, com.example.musicapp.ui.favorites.FavoriteSongsFragment.newInstance())
                .addToBackStack("FAVORITES")
                .commit()
        }

        // Toggle dark mode
        switchDarkMode.setOnClickListener {
            val newMode = !PreferenceHelper.isDarkMode(requireContext())
            PreferenceHelper.setDarkMode(requireContext(), newMode)
            PreferenceHelper.applyTheme(requireContext())
            activity?.recreate()
        }

        return view
    }

    private fun fetchUserData() {
        ApiClient.api.getUserProfile().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.data

                    // Gán danh sách playlist
                    val playlists = user.playlists ?: emptyList()
                    rvPlaylists.adapter = PlaylistAdapter(playlists).apply {
                        setOnItemClickListener { playlist ->
                            val fragment = PlaylistDetailFragment().apply {
                                arguments = Bundle().apply {
                                    putString("playlistId", playlist._id)
                                }
                            }
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack("PLAYLIST_DETAIL")
                                .commit()
                        }
                    }

                    // Gán danh sách songs, artists
                    val songs = user.follow_songs ?: emptyList()
                    val artists = user.follow_artists ?: emptyList()

                    rvSongs.adapter = SongAdapter(songs)
                    rvArtists.adapter = ArtistAdapter(artists)

                } else {
                    Toast.makeText(requireContext(), "Lỗi load dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchUserData()
        loadFavoriteSongs()
    }

    private fun loadFavoriteSongs() {
        favoriteRepository.getFavoriteSongs { songs, error, _ ->
            if (error == null && songs != null) {
                rvFavoriteSongs.adapter = SongAdapter(songs) { song ->
                    // TODO: xử lý khi click vào song
                }
            }
        }
    }
}
