package com.example.musicapp.ui.playlists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.playlists.PlaylistDetailResponse
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.home.SongAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaylistDetailFragment : Fragment() {
    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDesc: TextView
    private lateinit var rvSongs: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)
        ivCover = view.findViewById(R.id.ivCover)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDesc = view.findViewById(R.id.tvDesc)
        rvSongs = view.findViewById(R.id.rvSongs)

        rvSongs.layoutManager = LinearLayoutManager(requireContext())

        val playlistId = arguments?.getString("playlistId")
        if (playlistId != null) {
            fetchPlaylistDetail(playlistId)
        }

        return view
    }

    private fun fetchPlaylistDetail(playlistId: String) {
        ApiClient.api.getPlaylistDetail(playlistId).enqueue(object : Callback<PlaylistDetailResponse> {
            override fun onResponse(
                call: Call<PlaylistDetailResponse>,
                response: Response<PlaylistDetailResponse>
            ) {

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val playlist = data.playlist
                    val songs = data.songsInPlaylist

                    tvTitle.text = playlist.title
                    tvDesc.text = playlist.description
                    Glide.with(requireContext()).load(playlist.coverImage).into(ivCover)

                    rvSongs.adapter = SongAdapter(songs)
                } else {
                    Toast.makeText(requireContext(), "Lỗi load playlist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PlaylistDetailResponse>, t: Throwable) {
                Log.d("API_DEBUG", "API lỗi: ${t.message}")
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}