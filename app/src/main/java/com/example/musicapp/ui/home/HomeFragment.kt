package com.example.musicapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R

class HomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: SongAdapter
    private lateinit var viewModel: SongViewModel

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
            // Khi click 1 bài: tạm Toast, sau này mở DetailActivity/Fragment
            Toast.makeText(requireContext(), "Click: ${song.artist} ", Toast.LENGTH_SHORT).show()
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
    }
}
