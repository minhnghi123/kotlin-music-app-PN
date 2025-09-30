package com.example.musicapp.ui.suggestion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.models.songs.Song

class SuggestionGroupAdapter(
    private var groups: List<List<Song>>,
    private val onClick: (Song) -> Unit
) : RecyclerView.Adapter<SuggestionGroupAdapter.GroupVH>() {

    class GroupVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvGroupSongs: RecyclerView = itemView.findViewById(R.id.rvGroupSongs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion_group, parent, false)
        return GroupVH(view)
    }

    override fun onBindViewHolder(holder: GroupVH, position: Int) {
        val songs = groups[position]
        val adapter = SuggestionAdapter(songs, onClick) // reuse adapter cũ của bạn
        holder.rvGroupSongs.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.VERTICAL, false)
        holder.rvGroupSongs.adapter = adapter
    }

    override fun getItemCount() = groups.size

    fun submitSongs(songs: List<Song>) {
        groups = songs.chunked(3) // nhóm 3 bài 1 trang
        notifyDataSetChanged()
    }
}
