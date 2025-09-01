package com.example.musicapp.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.Song

class SongAdapter(
    private var items: List<Song> ,
    private val onClick: (Song) -> Unit ={}
):RecyclerView.Adapter<SongAdapter.SongVH>() {
    class SongVH(itemView:View): RecyclerView.ViewHolder(itemView) {
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtArtist: TextView = itemView.findViewById(R.id.txtArtist)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongVH(view)
    }
    override fun onBindViewHolder(holder: SongVH, position: Int) {
        val song = items[position]
        holder.txtTitle.text = song.title
        holder.txtArtist.text = song.artist.fullName
        Log.d("SongAdapter", "cover url = ${song.coverImage}")
        // Load ảnh cover
        Glide.with(holder.itemView)
            .load(song.coverImage)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.drawable.img_error) // Use a custom error image
            .into(holder.imgCover)
        Log.d("SongAdapter", "cover url = '${song.coverImage}'")

        holder.itemView.setOnClickListener { onClick(song) }
    }

    override fun getItemCount() = items.size
    @SuppressLint("NotifyDataSetChanged")
    fun submit(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }
}