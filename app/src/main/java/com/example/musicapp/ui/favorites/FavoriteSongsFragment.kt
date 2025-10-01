package com.example.musicapp.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private lateinit var tvEmptyState: TextView
    private lateinit var btnClearAll: Button
    private lateinit var adapter: SongAdapter
    private lateinit var repository: FavoriteSongsRepository
    private val playerVM: PlayerViewModel by activityViewModels()
    
    private var favoriteSongs: List<Song> = emptyList()
    
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
        repository = FavoriteSongsRepository()
    }
    
    private fun setupRecyclerView() {
        adapter = SongAdapter(favoriteSongs) { song ->
            playerVM.play(song)
        }
        
        // Set up favorite toggle functionality
        adapter.setOnAddToPlaylistClickListener { song ->
            toggleFavorite(song)
        }
        
        rvFavoriteSongs.layoutManager = LinearLayoutManager(requireContext())
        rvFavoriteSongs.adapter = adapter
    }
    
    private fun setupClickListeners() {
        btnClearAll.setOnClickListener {
            clearAllFavorites()
        }
    }

    private fun loadFavoriteSongs() {
        repository.getFavoriteSongs { songs, error, metadata ->
            if (error != null) {
                Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
                showEmptyState()
            } else {
                favoriteSongs = songs ?: emptyList()
                updateUI()
            }
        }
    }

    
    private fun toggleFavorite(song: Song) {
        repository.removeFavoriteSong(song._id) { success, message ->
            if (success) {
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
                loadFavoriteSongs() // Refresh the list
            } else {
                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun clearAllFavorites() {
        repository.removeAllFavoriteSongs { success, message ->
            if (success) {
                Toast.makeText(requireContext(), "All favorites cleared", Toast.LENGTH_SHORT).show()
                loadFavoriteSongs() // Refresh the list
            } else {
                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
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




