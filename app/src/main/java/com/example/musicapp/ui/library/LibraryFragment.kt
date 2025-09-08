package com.example.musicapp.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import android.widget.ImageView
import android.widget.Toast

class LibraryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LibraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerLibrary)

        val items = listOf(
            LibraryItem("Recently Played", R.drawable.ic_history),
            LibraryItem("Playlists", R.drawable.ic_playlist),
            LibraryItem("Albums", R.drawable.ic_album),
            LibraryItem("Favorites", R.drawable.ic_favorite)
        )

        val ivSettings = view.findViewById<ImageView>(R.id.iv_settings)

        ivSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        adapter = LibraryAdapter(items) { item ->
            when (item.title) {
                "Recently Played" -> {
                    // mở RecentlyPlayedFragment
                }
                "Playlists" -> {
                    // mở PlaylistsFragment
                }
                "Albums" -> {
                    // mở AlbumsFragment
                }
                "Favorites" -> {
                    // mở FavoritesFragment
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }
}

data class LibraryItem(val title: String, val iconRes: Int)
