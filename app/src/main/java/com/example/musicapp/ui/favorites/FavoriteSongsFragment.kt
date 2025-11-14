package com.example.musicapp.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.models.songs.Song
import com.example.musicapp.ui.home.SongAdapter
import com.example.musicapp.ui.player.PlayerViewModel

class FavoriteSongsFragment : Fragment() {
    
    private lateinit var rvFavoriteSongs: RecyclerView
    private lateinit var tvEmptyState: View
    private lateinit var btnClearAll: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnPlayAll: Button
    private lateinit var btnShuffle: Button
    private lateinit var tvSongCount: TextView
    private lateinit var adapter: FavoriteSongAdapter
    private lateinit var repository: FavoriteSongsRepository
    private val playerVM: PlayerViewModel by activityViewModels()
    
    private var favoriteSongs: List<Song> = emptyList()
    private var favoriteSongIds: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_songs, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadFavoriteSongs()
    }
    
    private fun initViews(view: View) {
        rvFavoriteSongs = view.findViewById(R.id.rvFavoriteSongs)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        btnClearAll = view.findViewById(R.id.btnClearAll)
        btnBack = view.findViewById(R.id.btnBack)
        btnPlayAll = view.findViewById(R.id.btnPlayAll)
        btnShuffle = view.findViewById(R.id.btnShuffle)
        tvSongCount = view.findViewById(R.id.tvSongCount)
        repository = FavoriteSongsRepository()
    }
    
    private fun setupRecyclerView() {
        adapter = FavoriteSongAdapter(favoriteSongs) { song ->
            // 👇 Update PlayerHolder + play
            com.example.musicapp.ui.player.PlayerHolder.currentSong = song
            playerVM.play(song)
        }
        
        adapter.setOnHeartClickListener { song ->
            toggleFavorite(song)
        }
        
        adapter.setOnAddToPlaylistClickListener { song ->
            // TODO: Add to playlist
        }
        
        rvFavoriteSongs.layoutManager = LinearLayoutManager(requireContext())
        rvFavoriteSongs.adapter = adapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        btnClearAll.setOnClickListener {
            showClearAllDialog()
        }
        
        btnPlayAll.setOnClickListener {
            if (favoriteSongs.isNotEmpty()) {
                // 👇 Update PlayerHolder + play
                com.example.musicapp.ui.player.PlayerHolder.currentSong = favoriteSongs.first()
                playerVM.play(favoriteSongs.first())
                Toast.makeText(requireContext(), "Playing all songs", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnShuffle.setOnClickListener {
            if (favoriteSongs.isNotEmpty()) {
                val shuffled = favoriteSongs.shuffled()
                // 👇 Update PlayerHolder + play
                com.example.musicapp.ui.player.PlayerHolder.currentSong = shuffled.first()
                playerVM.play(shuffled.first())
                Toast.makeText(requireContext(), "Shuffle play", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFavoriteSongs() {
        repository.getFavoriteSongs { songs, error, ids ->
            if (error == null && songs != null) {
                favoriteSongs = songs
                favoriteSongIds = ids ?: emptySet()
                
                // 👇 Update adapter với IDs để sync hearts
                adapter.updateFavoriteIds(favoriteSongIds)
                adapter.updateData(favoriteSongs)
                
                updateUI()
            } else {
                showEmptyState()
            }
        }
    }

    
    private fun toggleFavorite(song: Song) {
        repository.removeFavoriteSong(song._id) { success, _ ->
            if (success == true) {
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
                loadFavoriteSongs()
            }
        }
    }

    private fun showClearAllDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear all favorites?")
            .setMessage("This will remove all songs from your liked songs.")
            .setPositiveButton("Clear") { _, _ ->
                clearAllFavorites()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllFavorites() {
        repository.removeAllFavoriteSongs { success, _ ->
            if (success == true) {
                Toast.makeText(requireContext(), "All favorites cleared", Toast.LENGTH_SHORT).show()
                loadFavoriteSongs()
            }
        }
    }
    
    private fun updateUI() {
        if (favoriteSongs.isEmpty()) {
            showEmptyState()
        } else {
            showSongsList()
            adapter.updateData(favoriteSongs)
        }
    }
    
    private fun showEmptyState() {
        rvFavoriteSongs.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
        btnClearAll.visibility = View.GONE
    }
    
    private fun showSongsList() {
        rvFavoriteSongs.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        btnClearAll.visibility = View.VISIBLE
    }
    
    companion object {
        fun newInstance(): FavoriteSongsFragment {
            return FavoriteSongsFragment()
        }
    }
}




