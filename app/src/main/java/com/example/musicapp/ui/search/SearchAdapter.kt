package com.example.musicapp.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.songs.Song

class SearchAdapter(
    private var items: List<Song>,
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.ivSongCover)
        val tvTitle: TextView = itemView.findViewById(R.id.tvSongTitle)
        val tvArtist: TextView = itemView.findViewById(R.id.tvSongArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_search, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = items[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist.firstOrNull()?.fullName ?: "Unknown Artist"

        Glide.with(holder.itemView.context)
            .load(song.coverImage)
            .placeholder(R.drawable.ic_default_album_art)
            .into(holder.ivCover)

        holder.itemView.setOnClickListener { onItemClick(song) }
    }

    fun updateData(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }
}
