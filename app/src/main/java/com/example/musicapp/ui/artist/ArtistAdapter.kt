package com.example.musicapp.ui.artist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.models.artists.Artist
import com.example.musicapp.R
class ArtistAdapter(private val artists: List<Artist>) :
    RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.ivArtistCover)
        val tvName: TextView = itemView.findViewById(R.id.tvArtistName)
        val tvCountry: TextView = itemView.findViewById(R.id.tvArtistCountry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.tvName.text = artist.fullName
        holder.tvCountry.text = artist.country
        Glide.with(holder.itemView.context).load(artist.coverImage).into(holder.ivCover)
    }

    override fun getItemCount() = artists.size
}