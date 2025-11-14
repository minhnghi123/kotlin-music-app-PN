package com.example.musicapp.ui.playlists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.models.playlists.PlaylistDetailResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.favorites.FavoriteSongAdapter
import com.example.musicapp.ui.home.SongAdapter
import com.example.musicapp.ui.player.PlayerHolder
import com.example.musicapp.ui.player.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaylistDetailFragment : Fragment() {
    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDesc: TextView
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var btnPlayAll: Button
    private lateinit var btnShuffle: Button
    private lateinit var tvSongCount: TextView

    private val playerVM: PlayerViewModel by activityViewModels()
    private lateinit var favoriteRepository: FavoriteSongsRepository
    private var adapter: FavoriteSongAdapter? = null
    private var songs: List<Song> = emptyList()
    private var favoriteSongIds: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)
        ivCover = view.findViewById(R.id.ivCover)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvDesc = view.findViewById(R.id.tvDesc)
        rvSongs = view.findViewById(R.id.rvSongs)
        btnBack = view.findViewById(R.id.btnBack)
        btnPlayAll = view.findViewById(R.id.btnPlayAll)
        btnShuffle = view.findViewById(R.id.btnShuffle)
        tvSongCount = view.findViewById(R.id.tvSongCount)

        favoriteRepository = FavoriteSongsRepository()
        rvSongs.layoutManager = LinearLayoutManager(requireContext())

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnPlayAll.setOnClickListener {
            if (songs.isNotEmpty()) {
                // 👇 Update PlayerHolder + play
                PlayerHolder.currentSong = songs.first()
                playerVM.play(songs.first())
                Toast.makeText(requireContext(), "Playing all", Toast.LENGTH_SHORT).show()
            }
        }

        btnShuffle.setOnClickListener {
            if (songs.isNotEmpty()) {
                val shuffled = songs.shuffled()
                // 👇 Update PlayerHolder + play
                PlayerHolder.currentSong = shuffled.first()
                playerVM.play(shuffled.first())
                Toast.makeText(requireContext(), "Shuffle play", Toast.LENGTH_SHORT).show()
            }
        }

        val playlistId = arguments?.getString("playlistId")
        if (playlistId != null) {
            loadFavoriteSongs()
            fetchPlaylistDetail(playlistId)
        }

        return view
    }

    private fun loadFavoriteSongs() {
        favoriteRepository.getFavoriteSongs { _, _, ids ->
            favoriteSongIds = ids ?: emptySet()
            adapter?.updateFavoriteIds(favoriteSongIds)
        }
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
                    songs = data.songsInPlaylist

                    tvTitle.text = playlist.title
                    tvDesc.text = playlist.description
                    tvSongCount.text = "${songs.size} songs"

                    Glide.with(requireContext())
                        .load(playlist.coverImage)
                        .into(ivCover)

                    adapter = FavoriteSongAdapter(songs) { song ->
                        // 👇 Update PlayerHolder + play
                        PlayerHolder.currentSong = song
                        playerVM.play(song)
                    }

                    adapter?.setOnHeartClickListener { song ->
                        toggleFavorite(song)
                    }

                    adapter?.updateFavoriteIds(favoriteSongIds)
                    rvSongs.adapter = adapter
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

    private fun toggleFavorite(song: Song) {
        if (favoriteSongIds.contains(song._id)) {
            favoriteRepository.removeFavoriteSong(song._id) { success, _ ->
                if (success == true) {
                    loadFavoriteSongs()
                }
            }
        } else {
            favoriteRepository.addFavoriteSong(song._id) { success, _ ->
                if (success == true) {
                    loadFavoriteSongs()
                }
            }
        }
    }
}