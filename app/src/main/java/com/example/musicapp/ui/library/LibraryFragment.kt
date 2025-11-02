package com.example.musicapp.ui.library

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.ui.home.SongAdapter
import com.example.musicapp.data.FavoriteSongsRepository

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

        loadFavoriteSongs()

        view.findViewById<TextView>(R.id.tvFavHeader).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, com.example.musicapp.ui.favorites.FavoriteSongsFragment.newInstance())
                .addToBackStack("FAVORITES")
                .commit()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
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
